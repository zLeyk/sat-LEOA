package com.sat.utils;

import com.sat.satquery.entity.Leoleo;
import com.sat.satquery.entity.Preleo;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.*;

public class LEOTask implements Runnable {

    private Socket socket;
    String msg;


    public LEOTask(Socket socket) {
        this.socket = socket;
    }


    public void run() {
        try {
            handleSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 跟客户端Socket进行通信
     * @throws Exception
     */
    private void handleSocket() throws Exception {
        Preleo preleo = new Preleo();
        Leoleo leoleoB = new Leoleo();
        DESUtils ds = new DESUtils();
        //  注释是是查jdbc连接数据库查询数据表
        String url = "jdbc:mysql://localhost:3306/leoa";
        Connection connection = null;
        Statement statement = null;
        String sql = "";
        ResultSet resultSet2 = null;
        //  获得收到的信息
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "GBK"));
        StringBuilder sb = new StringBuilder();
        //socket.setSoTimeout(10*10000);
        String temp;
        int index;

        //设置超时间为10秒
        try {
            socket.setSoTimeout(10 * 1000);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }

        while ((temp=br.readLine()) != null) {
            if ((index = temp.indexOf("eof")) != -1) {//遇到eof时就结束接收
                sb.append(temp.substring(0, index));
                break;
            }

            sb.append(temp);
        }

        String s = sb.toString();// s是收到的信息
        System.out.println("Step2：");
        //System.out.println("B发广播信息，发给A的信息是:"+msg);
        System.out.println("LEO-A接收LEO-B信息："+s);


        //Step2 判断是不是地面跟自己通信如果是1则，是B发来的和自己的通信消息，首先对该卫星的 SSID进行识别。
        // 根据得到的SSIDB ，判断是二方通信还是三方通信
        String s1 = s.split(",")[0];
        if (s1.equals("1")){
            System.out.println("判断B发来的信息是否是和自己通信：是");
            String s2 = s.split(",")[1];
            Integer SSID_B = Integer.valueOf(s2);
            //System.out.println(SSID_B);
            //查询数据库表中的是否有SSID_B
            int exist;
            try {
                connection = DriverManager.getConnection(url, "root", "123456");
                statement = connection.createStatement();
                //sql = "select * from leoleo";
                sql = "select * from leoleo where SSID = " + SSID_B.toString();
                resultSet2 = statement.executeQuery(sql);
                exist = 0;
                while (resultSet2.next()) {
                    exist++;
                    leoleoB.setIDsat(resultSet2.getInt(1));
                    leoleoB.setSsid(resultSet2.getString(2));
                    leoleoB.setTida(resultSet2.getString(3));
                    leoleoB.setTidb(resultSet2.getString(4));
                    leoleoB.setSt(resultSet2.getInt(5));
                    leoleoB.setToken(resultSet2.getString(6));
                }
                resultSet2.close();

            } catch (SQLException e) {
                msg = "连接数据库失败";
                throw new RuntimeException(e);
            }
            // System.out.printf("%d", exist);

            //  查询自身预置表全部信息
            sql = "select * from preleo";
            ResultSet resultSet = null;
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                preleo.setIDsat(resultSet.getInt(1));
                //Integer ID_A = preleo.getIDsat();
                preleo.setSsid(resultSet.getInt(2));
                preleo.setDkenc(resultSet.getString(3));
                preleo.setDkauth(resultSet.getString(4));
                preleo.setWKenc(resultSet.getString(5));
                preleo.setK(resultSet.getString(6));
                preleo.setMainKey(resultSet.getString(7));
                preleo.setCk(resultSet.getString(8));

            }
            resultSet.close();


            if (exist == 0) {
                resultSet2.close();
                System.out.println("判断进行三方认证还是两方认证：三方认证");
                //如果三方认证

                //System.out.println(preleo);
                //创建一个和星地通信的线程 获得地面的IP 端口号 可以通过B发的广播信息传过来 让B在controller里面调用service也可以在类里面自己再写一个jdbc(因为连接的不同的数据库)
                //String host = ;
                //String port = ;
                // 传入Socket 和 卫星的预置信息(通过前面注解的JDBC查)和地面开始通信建立了一个新线程，查数据库
                //查询地面端口和IP地面端口
                Socket sockettcc = new Socket("127.0.0.1", 8899);
                LeoLeoAuthTask leoLeoAuthTask = new LeoLeoAuthTask(sockettcc, preleo, SSID_B);
                Thread thread = new Thread(leoLeoAuthTask);
                thread.start();
                thread.join();


                //和地面通信处理完之后也就是LeoLeoTccAuthTask执行完毕继续和B通信
                //怎么把数据拿出来
                String E_As = leoLeoAuthTask.getData();
                //System.out.println("拿出来了和地面通信建立的子线程的数据了");

                System.out.println("Step4");
                System.out.println("LEO-A接收TCC信息："+E_As);


                //Step3 根据A的会话密钥解密获得A的临时身份B的真实身份，临时身份真实身份和时间戳
                String E_A = ds.DESdecode(E_As, preleo.getCk());
                System.out.println("LEO-A解密信息："+E_A);
                String TID_A = E_A.split(",")[0] + "," + E_A.split(",")[1];
                String ID_B = E_A.split(",")[2];
                String TID_B = E_A.split(",")[3] + "," + E_A.split(",")[4];
                String T = E_A.split(",")[5];
                String E_B = E_A.split(",")[6];

                //验证时间戳新鲜
                Long tt = Long.parseLong(T);
                //当前时间
                Long ct = System.currentTimeMillis();
                if ((ct - tt) > 2000) {
                    System.out.println("认证失败");
                    statement.close();
                    connection.close();
                    br.close();
                    socket.close();
                    //返回一个认证失败的消息。
                }

                //生成新的请求
                String msg_B = "3," + preleo.getSsid().toString() + "," + TID_A + "," + E_B;

                //将信息发送给B
                Writer writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
                writer.write(msg_B);
                writer.write("eof\n");
                writer.flush();
                System.out.println("LEO-A向LEO-B发送信息："+msg_B);



                //三方认证Step 5  接受B返回的消息解密获得随机数，B临时身份，加密后的Token
                //通过A子线程SanfanServerReadThread接收来自B子线程写的消息解密获得随机数，B临时身份，加密后的Token
                //在线程里面计算认证操作
                //该监听端口是新创立的和Info里面的所有数据都不能一样
                // 只是表示子线程监听的端口，和B主线程里的三方认证子线程端口一样
                ServerSocket ssocket = new ServerSocket(9990); //实例化一个基于服务器端的socket对象
                Socket socket2 = ssocket.accept(); //调用监听功能，侦听9998端口，有信息就直接获得该客户端的socket对象
                SanfanServerReadThread sanfanServerReadThread = new SanfanServerReadThread(socket2, preleo, TID_B);//实例化子线程，用来取客户端发送的信息
                Thread thread2 = new Thread(sanfanServerReadThread);
                thread2.start(); //子线程启动
                thread2.join();
                writer.close();



                String Flag_Token = sanfanServerReadThread.getData();
                //System.out.println(Flag_Token);
                if (Flag_Token != "0") {

                    System.out.println("LEO-A向卫星认证表插入三方认证数据");
                    int IDsat_B = Integer.valueOf(ID_B);
                    int SSid_B = SSID_B;
                    try {
                        sql = "insert into leoleo values(?,?,?,?,?,?) ";
                        PreparedStatement pst = connection.prepareStatement(sql);//用来执行SQL语句查询，对sql语句进行预编译处理
                        pst.setInt(1, IDsat_B);
                        pst.setInt(2, SSid_B);
                        pst.setString(3, TID_A);
                        pst.setString(4, TID_B);
                        pst.setInt(5, 1);
                        pst.setString(6, Flag_Token);
                        pst.executeUpdate();//解释在下
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }


                    sockettcc.close();
                    socket2.close();
                    ssocket.close();
                    statement.close();
                    connection.close();
                    br.close();
                    socket.close();


                } else {
                    sockettcc.close();
                    socket2.close();
                    ssocket.close();
                    statement.close();
                    connection.close();
                    br.close();
                    socket.close();

                }


            } else {
                System.out.println("判断进行三方认证还是两方认证：二方认证");

                //二方认证  Step1 根据获得B——SSID获得leoelo表中IDsat为B的一行数据，将时间戳，临时身份A和 Token发给B

                //当前时间
                Long ct1 = System.currentTimeMillis();
                String ct = Long.toString(ct1);

                //生成新的请求
                String msg_B = "2" + "," + ct + ","+ preleo.getIDsat().toString() + ","+ leoleoB.getTida().toString() + "," + leoleoB.getToken();
                //System.out.println(msg_B);

                //将信息发送给B
                Writer writer = new OutputStreamWriter(socket.getOutputStream(), "GBK");
                writer.write(msg_B);
                writer.write("eof\n");
                writer.flush();
                System.out.println("LEO-A向LEO-B发送信息："+msg_B);


                //二方认证  Step3

                //读取来自B的消息，获得临时身份B，将临时身份B和之前Step1获得的临时身份B作比较
                System.out.println("Step4:");
                String TID_B2 = leoleoB.getTidb();

                //通过A子线程接收来自B子线程写的消息，子线程监听来自B的两方认证子线程端口
                ServerSocket ssocket = new ServerSocket(9991); //实例化一个基于服务器端的socket对象
                Socket socket2 = ssocket.accept(); //调用监听功能，侦听9991端口，有信息就直接获得该客户端的socket对象
                LiangfanServerReadThread liangfanServerReadThread = new LiangfanServerReadThread(socket2, TID_B2);//实例化子线程，用来取客户端发送的信息
                Thread thread3 = new Thread(liangfanServerReadThread);
                //new Thread(thread).start(); //实例化子线程对象，并启动子
                //由主线程来负责发送数据给客户端
                thread3.start();
                thread3.join();

                String Flag_liangfan = liangfanServerReadThread.getData();
                //System.out.println("两方之后的认证状态");
                //System.out.println(Flag_liangfan);
                if (Flag_liangfan.equals("0")){
                    //System.out.println("二次认证失败");
                }else {
                    //更改自己星星认证表的三方认证时候存的数据
                    sql = "UPDATE leoleo set ST = 1 WHERE IDsat = " + leoleoB.getIDsat().toString();
                    //System.out.println(sql);
                    boolean execute = statement.execute(sql);
                    System.out.println("二次认证成功，更改自身认证表成功");
                    //leoleoB.getIDsat();
//                    if(execute){
//                        System.out.println("二次认证成功，更改状态成功自身表");
//                    }else {
//                        System.out.println("二次认证成功，更改状态失败自身表");
//                    }

                    //改对方的数据

                    String urlleob = "jdbc:mysql://localhost:3306/leob";
                    Connection connectionleob = null;
                    Statement statementleob = null;
                    String sqlleob = "";
                    //ResultSet resultSetleob = null;
                    connectionleob = DriverManager.getConnection(urlleob,"root","123456");
                    statementleob = connectionleob.createStatement();
                    try {
                        //查询语句
                        sqlleob = "UPDATE leoleo set ST = 1 WHERE IDsat = " + preleo.getIDsat().toString();;
                        //System.out.println(sqlleob);
                        boolean executeleob = statementleob.execute(sqlleob);
                        System.out.println("二次认证成功，更改B认证表成功");

                        //resultSetleob.close();
                    } catch (SQLException e) {
                        msg = "连接数据库失败";
                        throw new RuntimeException(e);
                    }
                    connectionleob.close();
                    statementleob.close();



                }


                connection.close();
                statement.close();
                br.close();
                ssocket.close();
                socket2.close();
                socket.close();
            }
        }
        else {
            socket.close();
        }

}}



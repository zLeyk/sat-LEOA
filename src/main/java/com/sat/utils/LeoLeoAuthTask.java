package com.sat.utils;

import com.sat.satquery.entity.Leoleo;
import com.sat.satquery.entity.Preleo;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.sql.*;

//星认证流程
public class LeoLeoAuthTask implements Runnable {

    private Socket client;

    private boolean flag;
    private Preleo preleo;

    public LeoLeoAuthTask(Socket client, boolean flag, Preleo preleo) {
        this.client = client;
        this.preleo = preleo;
        this.flag = flag;
    }

    public void run() {
        try {
            handleSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void handleSocket() throws Exception {
        Leoleo leoleoB = new Leoleo();
        String msg;
        //PrintStream ps = new PrintStream("D:\\1.txt");

        String url = "jdbc:sqlite::resource:db/leoa.db";
        Class.forName("org.sqlite.JDBC");
        Connection connection = DriverManager.getConnection(url);
        Statement statement = connection.createStatement();

        //System.setOut(ps);
        System.out.println("本卫星预置信息表信息："+preleo);
        //ps.close();
        //加密工具 DES 密钥长度必须8的倍数 这个类要求必须8位
        DESUtils ds = new DESUtils();
        MD5Utils mds = new MD5Utils();

        //Step1 B发广播信息，给A的信息做了标注：标注+自身SSID
        //发送的消息 假设这个是发给A的消息 ，默认和他认证 可以在信息前面加个1标记 A收到这个信息会继续往下处理 其他卫星 收到信息 发现没有1 就不是和自己的认证 不处理
        if (flag = true){
            msg="1,"+preleo.getSsid();
            //System.out.println(msg);
            //B发送消息

            System.out.println("Step1：");
            System.out.println("LEO-A发广播信息，向LEO-B发送信息:"+msg);
            InputStream in1 =  client.getInputStream();
            OutputStream out1 = client.getOutputStream();
            InputStreamReader inputStreamReader1 = new InputStreamReader(in1,"UTF-8");
            OutputStreamWriter outputStreamWriter1 = new OutputStreamWriter(out1,"GBK");
            BufferedReader reader1 = new BufferedReader(inputStreamReader1);
            BufferedWriter writer1 = new BufferedWriter(outputStreamWriter1);
            writer1.write(msg);
            writer1.write("eof\n");
            writer1.flush();

            StringBuilder sb1 = new StringBuilder();
            String temp1;
            int index1;
            //设置超时间为10秒
            try {
                client.setSoTimeout(10 * 1000);
            } catch (SocketException e) {
                throw new RuntimeException(e);
            }
            while ((temp1=reader1.readLine()) != null) {
                if ((index1 = temp1.indexOf("eof")) != -1) {
                    sb1.append(temp1.substring(0, index1));
                    break;
                }
                sb1.append(temp1);
            }
            String s = sb1.toString();
            //System.out.println(s);

            // 三方认证 Step 4  根据消息前面的标记要是”3“进行三方认证，要是”2“执行两方认证代码

            if (s.split(",")[0].equals("3")){

                System.out.println("Step5:");
                System.out.println("LEO-B接收LEO-A信息："+s);

                /**
                 * Step4-1：LB 对认证请求中的密文信息进行解密，得到SSIDA、TIDA、RIDA、TIDB 、
                 * TT 共五个认证参数。如果该请求中的时间戳TT 满足新鲜性要求且该请求中的明
                 * 文身份信息 SSIDA + TIDA 与对方卫星所用的 SSIDA 以及 TCC 在密文中提供的
                 * TIDA相同，继续执行后续步骤；否者结束认证，释放连接。
                 */
                String SSID_A = s.split(",")[1];
                String TID_A = s.split(",")[2]+","+s.split(",")[3];
                String E_B = s.split(",")[4];
                String RE_B = ds.DESdecode(E_B,preleo.getCk());
                //System.out.println(RE_B);
                String SSID_A_EB = RE_B.split(",")[0];
                String TID_A_EB = RE_B.split(",")[1]+","+RE_B.split(",")[2];
                String ID_A_EB = RE_B.split(",")[3];
                String TID_B_EB = RE_B.split(",")[4]+","+RE_B.split(",")[5];
                String TT_EB = RE_B.split(",")[6];
                Long tt = Long.parseLong(TT_EB);
                //当前时间
                Long ct = System.currentTimeMillis();

                if((ct-tt)<2000 && SSID_A.equals(SSID_A_EB) && TID_A.equals(TID_A_EB)){

                    /**
                     * Step4-2： 获得随机数，通过主密钥加密（随机数，时间戳，B临时身份）获得MAC，
                     * 生成合成令牌（随机数，时间戳，B广播身份，MAC校验码）
                     * 计算CK会话密钥  通过主密钥加密随机数得到
                     * 计算XRES 预期响应数据
                     * 将加密后的随机数+TIDB+Token发送给A
                     */
                    //随机数R,8位的随机数，写了一个util---Randomget
                    //System.out.println("满足条件");
                    Randomget random1 = new Randomget();
                    String R = random1.getRandom1(8);

                    System.out.println("获得随机数："+R);
                    //生成Mac校验码
                    String MAC = ds.DESencode( R + Long.toString(ct) + TID_B_EB,preleo.getMainKey());
                    System.out.println("获得校验码："+MAC);
                    //生成合成令牌
                    String Token =R +","+ Long.toString(ct) +","+ preleo.getSsid().toString()+","+ MAC;
                    System.out.println("获得认证令牌："+Token);
                    //计算CK会话密钥,注意因为加密原因必须8位，CK取前8位置
                    String C_K = ds.DESencode(R,preleo.getMainKey());

                    String CK = C_K.substring(0, 8);
                    System.out.println("获得会话密钥："+CK);
                    //计算XRES 预期响应数据
                    String XRES = ds.DESencode(R,CK);
                    System.out.println("获得预期响应数据："+XRES);
                    String newmsg = R +","+ TID_B_EB +","+ ds.DESencode(Token,CK);


                    //第四步最后调用子线程SanfanClientReadThread，这样可以重新写数据不会出现收到的数据乱码，将B要给A的数据传给子线程，第六步在子线程了
                    //新线程采用的新端口号，和主线程的端口号不一样，表示和A子线程里面的通信
//                    //三方认证子线程的端口号
//                    Socket socket = new Socket("127.0.0.1",9990); // 实例化一个基于客户端的Socket对象，目标主机和目标主机的端口号，目标主机这里采用本电脑的ip
//                    SanfanClientReadThread sanfanclientReadThread = new SanfanClientReadThread(socket,newmsg,XRES);//实例化子线程：用来读取取服务器端信息
//                    Thread thread2 =new Thread(sanfanclientReadThread);
//                    thread2.start(); //子线程启动
//                    thread2.join();
                    InputStream in2 = client.getInputStream();
                    OutputStream out2 =client.getOutputStream();
                    InputStreamReader inputStreamReader2 =new InputStreamReader(in2);
                    OutputStreamWriter outputStreamWriter2 =new OutputStreamWriter(out2);
                    BufferedReader reader2 = new BufferedReader(inputStreamReader2);
                    BufferedWriter writer2 = new BufferedWriter(outputStreamWriter2);
                    writer2.write(newmsg);
                    writer2.write("eof\n");
                    writer2.flush();
                    System.out.println("LEO-B向LEO-A发送信息："+newmsg);
                    StringBuilder sb2 = new StringBuilder();
                    String temp2;
                    int index2;
                    //设置超时间为10秒
                    try {
                        client.setSoTimeout(10 * 1000);
                    } catch (SocketException e) {
                        throw new RuntimeException(e);
                    }

                    // 三方认证 Step 6  比较RES和XRES若一致认证成功，
                    while ((temp2=reader2.readLine()) != null) {
                        if ((index2 = temp2.indexOf("eof")) != -1) {
                            sb2.append(temp2.substring(0, index2));
                            break;
                        }
                        sb2.append(temp2);
                    }
                    String RES = sb2.toString();
                    System.out.println("Step7:");
                    System.out.println("LEO-B接收LEO-A信息："+RES);
                    if (RES.equals(XRES) ){
                        System.out.println("LEO-B校验数据");
                        System.out.println("三方认证成功");
                        //三方认证成功可以插入数据了
                        System.out.println("LEO-B向卫星认证表插入三方认证数据");
                        int ID_A = Integer.valueOf(ID_A_EB);
                        int ssid_A = Integer.valueOf(SSID_A);
                        try {
                            String sql = "insert into leoleo values(?,?,?,?,?,?) ";
                            PreparedStatement pst = connection.prepareStatement(sql);//用来执行SQL语句查询，对sql语句进行预编译处理
                            pst.setInt(1, ID_A);
                            pst.setInt(2, ssid_A);
                            pst.setString(3, TID_A_EB);
                            pst.setString(4, TID_B_EB);
                            pst.setInt(5, 1);
                            pst.setString(6, Token);
                            pst.executeUpdate();//解释在下
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        //告诉A三方认证成功了可以插入数据了
                        InputStream in3 = client.getInputStream();
                        OutputStream out3 =client.getOutputStream();
                        InputStreamReader inputStreamReader3 =new InputStreamReader(in3);
                        OutputStreamWriter outputStreamWriter3 =new OutputStreamWriter(out3);
                        BufferedReader reader3 = new BufferedReader(inputStreamReader3);
                        BufferedWriter writer3 = new BufferedWriter(outputStreamWriter3);
                        String Res = "YES";
                        writer3.write(Res);
                        writer3.write("eof\n");
                        writer3.flush();
                        System.out.println("LEO-A向LEO-B发送信息：LEO-B端三次认证成功");
                        writer3.close();
                        reader3.close();

                    }else {
                        System.out.println("三方认证失败");
                    }

                    reader1.close();
                    reader2.close();
                    writer1.close();
                    writer2.close();
                    statement.close();
                    connection.close();
                    client.close();

                }else {
                    System.out.println("三方认证失败");
                    reader1.close();
                    writer1.close();
                    statement.close();
                    connection.close();
                    client.close();
                    //返回一个认证失败的消息。
                }
            }
            else if(s.split(",")[0].equals("2")){
                //二方认证 Step2 获得来自A的数据，根据时间戳，A临时身份，Token，比较，并将B自身数据发送给A
                System.out.println("Step3:");
                System.out.println("LEO-B接收LEO-A信息："+s);

                String T = s.split(",")[1];
                String ID_A = s.split(",")[2];
                String TID_A = s.split(",")[3]+ ","+s.split(",")[4];
                String Token = s.split(",")[5]+ ","+s.split(",")[6]+ ","+s.split(",")[7]+ ","+s.split(",")[8];
                //String TID_A = s.split(",")[2]+","+s.split(",")[3];
                //String Token = s.split(",")[4]+","+s.split(",")[5]+","+s.split(",")[6]+","+s.split(",")[7];
                // 查找自身认证表中的存的Idsat为A的那一行临时身份A和Token，以及临时身份B
                String sql2 = "select * from leoleo where IDsat = "+ ID_A.toString();
                ResultSet resultSet2 = null;
                resultSet2 = statement.executeQuery(sql2);
                while (resultSet2.next()) {
                    leoleoB.setIDsat(resultSet2.getInt(1));
                    leoleoB.setSsid(resultSet2.getString(2));
                    leoleoB.setTida(resultSet2.getString(3));
                    leoleoB.setTidb(resultSet2.getString(4));
                    leoleoB.setSt(resultSet2.getInt(5));
                    leoleoB.setToken(resultSet2.getString(6));

                }
                resultSet2.close();
                //System.out.println(leoleoB.getTida());

                //比较时间戳和临时身份和Token，符合条件继续认证，不然认证失败
                Long t = Long.parseLong(T);
                //当前时间
                Long ct = System.currentTimeMillis();
                //(ct-t)<2000 &&

                if(  TID_A.equals(leoleoB.getTida()) && Token.equals(leoleoB.getToken())){


                    String newmsg_A = leoleoB.getTidb() ;
                    System.out.println("LEO-B校验数据");
                    //System.out.println(newmsg_A);
//-------------------------------
  //                  调用子线程，这样可以重新写数据，将B临时身份发送给A,其他两方认证就在子线程里面完成啦，这个端口号实例化一个吧
    //                二方认证子线程的端口
//                    Socket socket = new Socket("127.0.0.1",9991); // 实例化一个基于客户端的Socket对象，目标主机和目标主机的端口号，目标主机这里采用本电脑的ip
//                    LiangfanClientReadThread liangfanClientReadThread = new LiangfanClientReadThread(socket,newmsg_A);//实例化子线程：用来读取取服务器端信息
//                    Thread thread2 =new Thread(liangfanClientReadThread);
//                    thread2.start(); //子线程启动
//                    thread2.join();
//-------------------------------

                    InputStream in2 = client.getInputStream();
                    OutputStream out2 =client.getOutputStream();
                    InputStreamReader inputStreamReader2 =new InputStreamReader(in2);
                    OutputStreamWriter outputStreamWriter2 =new OutputStreamWriter(out2);
                    BufferedReader reader2 = new BufferedReader(inputStreamReader2);
                    BufferedWriter writer2 = new BufferedWriter(outputStreamWriter2);
                    writer2.write(newmsg_A);
                    writer2.write("eof\n");
                    writer2.flush();
                    System.out.println("LEO-B向LEO-A发送信息："+newmsg_A);

                    StringBuilder sb2 = new StringBuilder();
                    String temp2;
                    int index2;
                    //设置超时间为10秒
                    try {
                        client.setSoTimeout(10 * 1000);
                    } catch (SocketException e) {
                        throw new RuntimeException(e);
                    }

                    // 三方认证 Step 6  比较RES和XRES若一致认证成功，
                    while ((temp2=reader2.readLine()) != null) {
                        if ((index2 = temp2.indexOf("eof")) != -1) {
                            sb2.append(temp2.substring(0, index2));
                            break;
                        }
                        sb2.append(temp2);
                    }
                    String Res = sb2.toString();
                    if (Res.equals("YES")){
                        System.out.println("Step5:");
                        System.out.println("LEO-B接收LEO-A数据：LEO-A端二次认证成功");
                        //改自身的数据

                        try {
                            //查询语句
                            String sql3 = "UPDATE leoleo set ST = 1 WHERE IDsat = " + ID_A;;
                            //System.out.println(sqlleob);
                            boolean executeleob = statement.execute(sql3);
                            System.out.println("二次认证成功，更改LEO-B认证表成功");

                            //resultSetleob.close();
                        } catch (SQLException e) {
                            msg = "连接数据库失败";
                            throw new RuntimeException(e);
                        }

                    }else {
                        System.out.println("二次认证失败");
                    }

                    writer1.close();
                    writer2.close();
                    reader1.close();
                    reader2.close();
                    statement.close();
                    connection.close();
                    client.close();
                }else {
                    System.out.println("二方认证失败");
                    writer1.close();
                    reader1.close();
                    statement.close();
                    connection.close();
                    client.close();
                }

                }
        }else {

            //将消息发给其他不是A的客户端
            msg=preleo.getSsid().toString();
            //B发送消息
            Writer writer = null;
            BufferedReader br = null;
            writer = new OutputStreamWriter(client.getOutputStream(), "GBK");
            writer.write(msg);
            writer.write("eof\n");
            writer.flush();

            writer.close();
            statement.close();
            connection.close();
            client.close();

            //client.setSoTimeout(10*1000);  //设置一个超时 如果这个线程后面收到信息 说明是A发过来的 继续后面的处理
        }


    }
}


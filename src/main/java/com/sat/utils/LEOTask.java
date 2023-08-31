package com.sat.utils;

import com.sat.satquery.entity.Leoleo;
import com.sat.satquery.entity.Preleo;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.*;

//作为被认证的卫星， 处理认证任务
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
    private void handleSocket() throws ClassNotFoundException, SQLException, InterruptedException {
        Preleo preleo = new Preleo();
        Leoleo leoleo = new Leoleo();
        DESUtils ds = new DESUtils();
        //  注释是是查jdbc连接数据库查询数据表
        Class.forName("org.sqlite.JDBC");
        String url = "jdbc:sqlite::resource:db/leoa.db";
        Connection connection = null;
        Statement statement = null;
        String sql = "";
        ResultSet resultSet = null;
        BufferedReader reader = null;
        BufferedWriter writer = null;
        StringBuilder sb = new StringBuilder();
        String temp;
        int index;
        //  获得收到的信息  写到的时候在处理

        //socket.setSoTimeout(10*10000);


        //设置超时间为10秒
        try {
            socket.setSoTimeout(10 * 1000);
        } catch (SocketException e) {
        }
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "GBK"));
            while ((temp=reader.readLine()) != null) {
                if ((index = temp.indexOf("eof")) != -1) {//遇到eof时就结束接收
                    sb.append(temp.substring(0, index));
                    break;
                }
                sb.append(temp);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String s = sb.toString();// s是收到的信息
        System.out.println("Step2:");
        //System.out.println("B发广播信息，发给A的信息是:"+msg);

        //认证请求方的SSID
        Integer SSID = Integer.valueOf(s);
        //查询数据库表中的是否有SSID_B
        int exist;
        connection = DriverManager.getConnection(url);
        statement = connection.createStatement();
        //sql = "select * from leoleo";
//        sql = "delete from leoleo";
//        boolean r1 = statement.execute(sql);
        sql = "select * from leoleo where SSID = " + SSID;
        resultSet = statement.executeQuery(sql);
        exist = 0;
        while (resultSet.next()) {
            exist++;
            leoleo.setIDsat(resultSet.getString(1));
            leoleo.setSsid(resultSet.getInt(2));
            leoleo.setTidSrc(resultSet.getString(3));
            leoleo.setTidDst(resultSet.getString(4));
            leoleo.setSt(resultSet.getInt(5));
            leoleo.setToken(resultSet.getString(6));
        }
        resultSet.close();

        // System.out.printf("%d", exist);

        // 查询自身预置表全部信息
        sql = "select * from preleo";
        resultSet = statement.executeQuery(sql);
        while (resultSet.next()) {
            preleo.setIDsat(resultSet.getString(1));
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
        //没有查到SRC的信息
        if (exist == 0) {
            //如果三方认证
            TccLeoLeoAuthTask leoLeoAuthTask = null;
            Socket sockettcc = null;
            Thread thread = null;
            try {
                sockettcc = new Socket("127.0.0.1", 8899);
                //三方认证的星地认证  传入socket 自身得预置信息 SSID
                leoLeoAuthTask = new TccLeoLeoAuthTask(sockettcc, preleo, SSID);
                thread = new Thread(leoLeoAuthTask);
                thread.start();

            }  catch (UnknownHostException e) {
            } catch (IOException e) {
            }
            thread.join();
            try {
                sockettcc.close();
            } catch (IOException e) {
            }
            //和地面通信处理完之后也就是LeoLeoTccAuthTask执行完毕继续和B通信
            //怎么把数据拿出来
            //目的卫星和地面通信的信息 包括认证流程
            String tccmsg = leoLeoAuthTask.getData();
            //如果和地面通信失败  关闭   和地面通信 Leo发送信息 Tcc接收信息 在TccLeoLeoAuthTask处理 接收的信息 在这个类处理
            // 如果前两步出现错误，tccmsg的log里面会出现认证失败，tccmsg包括返回信息和认证流程，如果认证失败 只存在认证流程
            if(tccmsg.contains("失败")){
                try {
                    socket.close();
                } catch (IOException e) {
                }
            } // 地面通信成功  并返回接收的信息
            else {
                System.out.println("Step4");
                System.out.println("DST接收TCC信息:" + tccmsg);

                //Step3 根据DST的会话密钥解密获得DST的临时身份SRC的真实身份，临时身份真实身份和时间戳
                String E_Dst = null;
                try {
                    E_Dst = ds.DESdecode(tccmsg.split(",")[0], preleo.getCk());
                } catch (Exception e) {
                    try {
                        socket.close();
                    } catch (IOException ee) {
                    }
                }
                if(E_Dst!=null && !E_Dst.equals("")) {   //防止预置卫星的CK不足8位 报错
                    //解密和TCC通信获得的信息
                    if (E_Dst.split(",").length != 7) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                        }
                    } else {   //  解密信息
                        System.out.println("LEO-B解密信息：" + E_Dst);
                        String TID_Dst = E_Dst.split(",")[0] + "," + E_Dst.split(",")[1];
                        String ID_Src = E_Dst.split(",")[2];
                        String TID_Src = E_Dst.split(",")[3] + "," + E_Dst.split(",")[4];
                        String T = E_Dst.split(",")[5];
                        String E_Src = E_Dst.split(",")[6];
                        //验证时间戳新鲜
                        Long tt = Long.parseLong(T);
                        //当前时间
                        Long ct = System.currentTimeMillis();
                        //地面返回的信息 时间戳太大
                        if ((ct - tt) > 2000) {
                            System.out.println("认证失败");
                            statement.close();
                            connection.close();
                            try {
                                reader.close();
                                socket.close();
                            } catch (IOException e) {
                            }
                            //和源卫星继续认证 发送一个新的请求信息
                        } else {
                            String msg_Src = null;
                            try {
                                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "GBK"));
                                //生成新的请求
                                msg_Src = "3," + preleo.getSsid().toString() + "," + TID_Dst + "," + E_Src;
                                //将信息发送给B
                                writer.write(msg_Src);
                                writer.write("eof\n");
                                writer.flush();
                            } catch (Exception e) {
                            }
                            System.out.println("目的卫星向源卫星发送信息:" + msg_Src);
                            //

                            //Step4
                            System.out.println("Step6:");
                            //设置超时间为10秒
                            try {
                                socket.setSoTimeout(10 * 1000);
                            } catch (SocketException e) {
                            }



                            sb.setLength(0);
                            try {
                                while ((temp = reader.readLine()) != null) {
                                    if ((index = temp.indexOf("eof")) != -1) {
                                        sb.append(temp.substring(0, index));
                                        break;
                                    }
                                    sb.append(temp);
                                }
                            } catch (Exception e) {

                            }
                            //收到源卫星的信息
                            String msgsrc = sb.toString();

                            if (msgsrc!=null && !msgsrc.equals("")) {  // 如果收到了信息
                                String R = msgsrc.split(",")[0];
//                        String TID_Src2 = msgsrc.split(",")[1] + "," + msgsrc.split(",")[2];

                                //计算CK会话密钥,注意因为加密原因必须8位，CK取前8位置,和B中过程一样的目的就是得到和B一样的CK
                                String C_K = null;
                                try {
                                    C_K = ds.DESencode(R, preleo.getMainKey());
                                } catch (Exception e) {

                                }
                                if (C_K!=null && !C_K.equals("")) {   // 防止计算会话错误
                                    //System.out.println(C_K);
                                    String CK = "";
                                    if(C_K.length() >= 8) {
                                        CK = C_K.substring(0, 8);
                                        //System.out.println(CK);
                                    }else {
                                        CK = C_K + 'a'*(8-C_K.length());
                                    }
                                    //利用CK进行解密Token获得时间戳和校验码，比较时间戳和校验码是否一致
                                    String Token = null;
                                    try {
                                        Token = ds.DESdecode(msgsrc.split(",")[3], CK);
                                    } catch (Exception e) {
                                    }
                                    if (Token!=null && !Token.equals("")) {
                                        //System.out.println(Token);
                                        String Tt = Token.split(",")[1];
                                        String MAC = Token.split(",")[3];

                                        //计算MAC
                                        String XMAC = null;
                                        try {
                                            XMAC = ds.DESencode(R + Tt + TID_Src, preleo.getMainKey());
                                        } catch (Exception e) {
                                        }
                                        if (XMAC!=null && !XMAC.equals("")) {  //防止Mainkey不足8位报错
                                            System.out.println("获得校验码:" + XMAC);
                                            long Ttt = System.currentTimeMillis();
                                            if ((Ttt - Long.parseLong(Tt) < 2000) && MAC.equals(XMAC)) {
                                                //计算XRES 预期响应数据
                                                System.out.println("LEO-B校验数据");
                                                String RES = null;
                                                try {
                                                    RES = ds.DESencode(R, CK);
                                                    writer.write(RES);
                                                    writer.write("eof\n");
                                                    writer.flush();
                                                } catch (Exception e) {
                                                }

                                                System.out.println("LEO-B向LEO-A发送信息:" + RES);
                                                sb.setLength(0);
                                                //设置超时间为10秒
                                                try {
                                                    socket.setSoTimeout(10 * 1000);
                                                    while ((temp = reader.readLine()) != null) {
                                                        if ((index = temp.indexOf("eof")) != -1) {
                                                            sb.append(temp.substring(0, index));
                                                            break;
                                                        }
                                                        sb.append(temp);
                                                    }
                                                } catch (SocketException e) {
                                                } catch (IOException e) {
                                                }
                                                //三方认证第四步，接受来自B的数据
                                                msgsrc = sb.toString();
                                                if(msgsrc!=null && !msgsrc.equals("")) {  //防止对方验证错误 不发送信息 ，此时读取的信息为空
                                                    if (msgsrc.equals("YES")) {

                                                        String IDsat_Src = ID_Src;
                                                        try {
                                                            sql = "insert into leoleo(IDsat,SSID,TidSrc,TidDst,st,token,log) values(?,?,?,?,?,?,?) ";
                                                            PreparedStatement pst = connection.prepareStatement(sql);//用来执行SQL语句查询，对sql语句进行预编译处理
                                                            pst.setString(1, IDsat_Src);
                                                            pst.setInt(2, SSID);
                                                            pst.setString(3, TID_Src);
                                                            pst.setString(4, TID_Dst);
                                                            pst.setInt(5, 1);
                                                            pst.setString(6, Token);
                                                            pst.setString(7,"目的卫星认账成功");
                                                            pst.executeUpdate();
                                                            connection.close();
                                                        } catch (SQLException e) {
                                                            e.printStackTrace();
                                                        }
                                                    } else {
                                                        System.out.println("三方认证失败");
                                                    }
                                                }else {
                                                    System.out.println("认证失败");
                                                    try {
                                                        socket.close();
                                                        connection.close();
                                                    } catch (IOException e) {

                                                    }
                                                }

                                            }else {
                                                try {
                                                    socket.close();
                                                    connection.close();
                                                } catch (IOException e) {

                                                }
                                            }
                                        } else {
                                            System.out.println("三方认证失败");
                                            try {
                                                writer.close();
                                                connection.close();
                                                socket.close();
                                            } catch (IOException e) {
                                            }
                                        }

                                        statement.close();
                                        connection.close();
                                        try {
                                            reader.close();
                                            writer.close();
                                            socket.close();
                                        } catch (IOException e) {

                                        }
                                    }else {
                                        try {
                                            socket.close();

                                            connection.close();
                                        } catch (IOException e) {
                                        }
                                    }
                                }else {
                                    try {
                                        socket.close();

                                        connection.close();
                                    } catch (IOException e) {

                                    }
                                }
                            }else {
                                try {
                                    socket.close();

                                    connection.close();
                                } catch (IOException e) {

                                }
                            }
                        }
                    }
                }else {
                    try {

                        connection.close();
                        socket.close();
                    } catch (IOException e) {

                    }
                    System.out.println("CK错误");
                }
            }
        }
        else {
            System.out.println("判断进行三方认证还是两方认证：二方认证");

            //二方认证  Step1 根据获得B——SSID获得leoelo表中IDsat为B的一行数据，将时间戳，临时身份A和 Token发给B

            //当前时间
            Long ct1 = System.currentTimeMillis();
            String ct = Long.toString(ct1);
            System.out.println(preleo);
            System.out.println(leoleo);
            //生成新的请求
            String msg_Dst = "2" + "," + ct + "," + preleo.getIDsat() + "," + leoleo.getTidDst() + "," + leoleo.getToken();
            //System.out.println(msg_B);

            //将信息发送给B
            try {
                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                writer.write(msg_Dst);
                writer.write("eof\n");
                writer.flush();
            } catch (IOException e) {
            }

            System.out.println("LEO-B向LEO-A发送信息：" + msg_Dst);

            //二方认证  Step3

            //读取来自B的消息，获得临时身份B，将临时身份B和之前Step1获得的临时身份B作比较
            System.out.println("Step4:");
            String TID_Src = leoleo.getTidSrc();
            //System.out.println("来到两方认证子线程完成剩余两方认证操作");
            //在子线程继续而二方认证第三步


            sb.setLength(0);
            try {
                socket.setSoTimeout(10 * 1000);
                while ((temp = reader.readLine()) != null) {
                    if ((index = temp.indexOf("eof")) != -1) {
                        sb.append(temp.substring(0, index));
                        break;
                    }
                    sb.append(temp);
                }
            } catch (SocketException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {

            }

            String newTID_Src = sb.toString();
            System.out.println("LEO-B接收LEO-A信息：" + newTID_Src);
            if (newTID_Src!=null && !newTID_Src.equals("")) {
                if (newTID_Src.equals(leoleo.getTidSrc())) {
                    System.out.println("LEO-B校验数据");
                    System.out.println("二次认证成功");
                    //更改自己星星认证表的三方认证时候存的数据
                    sql = "UPDATE leoleo set ST = 1 WHERE IDsat = '" + leoleo.getIDsat() + "'";
                    boolean execute = statement.execute(sql);
                    System.out.println("二次认证成功，更改LEO-B认证表成功");
                    String massage_Dst = "YES";
                    try {
                        writer.write(massage_Dst);
                        writer.write("eof\n");
                        writer.flush();
                    } catch (IOException e) {

                    }
                    System.out.println("LEO-B向LEO-A发送信息：LEO-B端二次认证成功");
                    connection.close();
                    statement.close();
                    try {
                        socket.close();
                    } catch (IOException e) {

                    }

                } else {
                    System.out.println("LEO-B校验数据");
                    System.out.println("二次认证失败");

                    sql = "UPDATE leoleo set ST = 0 WHERE IDsat = '" + leoleo.getIDsat() + "'";
                    boolean execute = statement.execute(sql);
                    sql = "delete from leoleo where IDsat =  '" + leoleo.getIDsat() + "'";
                    boolean i = statement.execute(sql);
                    statement.close();
                    connection.close();
                    try {
                        socket.close();
                    } catch (IOException e) {

                    }
                    //return "0";
                }

            }else {
                sql = "UPDATE leoleo set ST = 0 WHERE IDsat = '" + leoleo.getIDsat() + "'";
                boolean execute = statement.execute(sql);
                sql = "delete from leoleo where IDsat =  '" + leoleo.getIDsat() + "'";
                boolean execute1 = statement.execute(sql);
                statement.close();
                connection.close();
                try {
                    socket.close();
                } catch (IOException e) {

                }
            }
        }
    }


}



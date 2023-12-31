package com.sat.utils;

import com.sat.satquery.entity.Leoleo;
import com.sat.satquery.entity.Preleo;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.sql.*;
import java.util.concurrent.CountDownLatch;

//作为发起认证卫星 认证流程
public class LeoLeoAuthTask implements Runnable {

    private Socket client;

    private String DstIDsat;
    private Preleo preleo;

    CountDownLatch latch;

    public LeoLeoAuthTask(Socket client, String IDsat, Preleo preleo, CountDownLatch latch) {
        this.client = client;
        this.preleo = preleo;
        this.DstIDsat = IDsat;
        this.latch = latch;
    }

    public void run() {
        try {
            handleSocket();
            latch.countDown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void handleSocket() throws SQLException, ClassNotFoundException {
        Leoleo leoleo = new Leoleo();
        String msg;
        String log;
        String temp;
        String sql = null;
        ResultSet resultSet = null;
        ResultSet resultSet1 = null;
        int index;
        BufferedReader reader = null;
        Writer writer = null;
        StringBuilder sb = new StringBuilder();
        String url = "jdbc:sqlite::resource:db/leoa.db";
        Class.forName("org.sqlite.JDBC");
        Connection connection = DriverManager.getConnection(url);
        Statement statement = connection.createStatement();
        System.out.println("本卫星预置信息表信息：" + preleo);
        String SrcIDsat = preleo.getIDsat();
        //加密工具 DES 密钥长度必须8的倍数 这个类要求必须8位
        DESUtils ds = new DESUtils();
        String clientIP = client.getInetAddress().getHostAddress();  //被认证卫星IP
        IptablesManager iptablesManager = new IptablesExample();
        //Step1 第一步 源卫星发送自己的广播编号到目的卫星
        try {
            client.setSoTimeout(10 * 1000);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        msg = preleo.getSsid().toString()+","+preleo.getIDsat();   //源卫星的SSID  发送信息

        try {
            writer = new OutputStreamWriter(client.getOutputStream(), "UTF-8");
            writer.write(msg);
            writer.write("eof\n");
            writer.flush();
        } catch (IOException e) {
        }
        System.out.println("Step1:");
        log = "Step1:\n";
        System.out.println("卫星" + SrcIDsat + "向卫星" + DstIDsat + "发起星间认证，发送自身广播编号:" + preleo.getSsid().toString());
        log += "卫星" + SrcIDsat + "向卫星" + DstIDsat + "发起星间认证，发送自身广播编号:" + preleo.getSsid().toString() + "\n";

        //接收信息，可能来自三方认证的信息和两方认证的信息  如果没有接收到信息 三方认证 说明目的卫星和地面端通信失败
        try {
            reader = new BufferedReader(new InputStreamReader(client.getInputStream(), "GBK"));
            while ((temp = reader.readLine()) != null) {
                if ((index = temp.indexOf("eof")) != -1) {
                    sb.append(temp.substring(0, index));
                    break;
                }
                sb.append(temp);
            }
        } catch (Exception e) {
        }
        //设置超时间为10秒

        String s = sb.toString();
        if(s.equals("")){    //没有接收到信息，可能的情况(1.三方认证目的卫星和地面卫星通信失败;2.)
            log += "认证失败\n";
            sql = "delete from leoleo where IDsat = '" + DstIDsat + "'";
            statement.execute(sql);
            //   iptablesManager.DacceptIcmp(clientIP);//封禁ip
            try {
                sql = "insert into leoleo(IDsat,st,log) values(?,?,?)";
                PreparedStatement pst = connection.prepareStatement(sql);//用来执行SQL语句查询，对sql语句进行预编译处理
                pst.setString(1, DstIDsat);
                pst.setInt(2, 0);
                pst.setString(3, log);
                pst.executeUpdate();
            } catch (SQLException ee) {
                ee.printStackTrace();
            }
            try {
                client.close();
            } catch (IOException ex) {

            }
        }
        //System.out.println(s);
        // 三方认证 Step 4  根据消息前面的标记要是”3“进行三方认证，要是”2“执行两方认证代码
        else {
            log += "Step2:\n";
            if (s.split(",")[0].equals("3")) {
                sql = "delete from leoleo where IDsat = '" + DstIDsat + "'";
                statement.execute(sql);
                log += "三方认证\n";
                log += "卫星"+DstIDsat+"和地面通信\n";
                if (s.contains("错误") || s.contains("非法") || s.contains("失败")) {
                    log += s.split(",")[1];
                    System.out.println(log);
                    log += "\n";
                    if(s.contains("密钥K错误") || s.contains("会话密钥错误")){
                        log += "认证失败\n";
                    }else
                        log += "三方认证失败\n";
                    sql = "insert into leoleo(IDsat,st,log) values(?,?,?)";
                    //   iptablesManager.DacceptIcmp(clientIP);//封禁ip
                    PreparedStatement pst = connection.prepareStatement(sql);//用来执行SQL语句查询，对sql语句进行预编译处理
                    pst.setString(1, DstIDsat);
                    pst.setInt(2, 0);
                    pst.setString(3, log);
                    pst.executeUpdate();
                    pst.close();
                } else {
                    log += "卫星" + DstIDsat + "与地面端通信成功\n";
                    log += "卫星" + DstIDsat + "向卫星" + SrcIDsat + "发送信息:" + s + "\n";
                    System.out.println("Step3:");
                    log += "Step3:\n";
                    System.out.println("卫星" + SrcIDsat + "接收卫星" + DstIDsat + "信息:" + s);
                    log += "卫星" + SrcIDsat + "接收卫星" + DstIDsat + "信息:" + s + "\n";
                    /**
                     * Step4-1：LB 对认证请求中的密文信息进行解密，得到SSIDA、TIDA、RIDA、TIDB 、
                     * TT 共五个认证参数。如果该请求中的时间戳TT 满足新鲜性要求且该请求中的明
                     * 文身份信息 SSIDA + TIDA 与对方卫星所用的 SSIDA 以及 TCC 在密文中提供的
                     * TIDA相同，继续执行后续步骤；否者结束认证，释放连接。
                     */
                    String SSID_Dst = s.split(",")[1];
                    String TID_Dst = s.split(",")[2] + "," + s.split(",")[3];
                    String E_Src = s.split(",")[4];
                    String RE_Src = null;
                    try {
                        RE_Src = ds.DESdecode(E_Src, preleo.getCk());
                    } catch (Exception e) {

                    }
                    if (RE_Src != null && !RE_Src.equals("")) {

                        if (RE_Src.split(",").length != 7) {
                            log += "校验信息失败\n";
                            log += "认证失败\n";
                            sql = "insert into leoleo(IDsat,st,log) values(?,?,?)";
                            PreparedStatement pst = connection.prepareStatement(sql);//用来执行SQL语句查询，对sql语句进行预编译处理
                            pst.setString(1, DstIDsat);
                            pst.setInt(2, 0);
                            pst.setString(3, log);
                            pst.executeUpdate();
                            //   iptablesManager.DacceptIcmp(clientIP);//封禁ip
                            try {
                                client.close();
                            } catch (IOException ex) {
                            }
                        } else {
                            String SSID_Dst_ESrc = RE_Src.split(",")[0];
                            String TID_Dst_ESrc = RE_Src.split(",")[1] + "," + RE_Src.split(",")[2];
                            String ID_Dst_ESrc = RE_Src.split(",")[3];
                            String TID_Src_ESrc = RE_Src.split(",")[4] + "," + RE_Src.split(",")[5];
                            String TT_ESrc = RE_Src.split(",")[6];
                            Long tt = Long.parseLong(TT_ESrc);
                            //当前时间
                            Long ct = System.currentTimeMillis();
                            if ((ct - tt) < 10000 && SSID_Dst.equals(SSID_Dst_ESrc) && TID_Dst.equals(TID_Dst_ESrc)) {

                                Randomget random1 = new Randomget();
                                String R = random1.getRandom1(8);

                                System.out.println("获得随机数：" + R);
                                //生成Mac校验码
                                String MAC = null;
                                try {
                                    MAC = ds.DESencode(R + Long.toString(ct) + TID_Src_ESrc, preleo.getMainKey());
                                } catch (Exception e) {
                                    System.out.println("Mankey错误");
                                    System.out.println("MAC:" + MAC);
                                }
                                if (MAC != null && !MAC.equals("")) {

                                    System.out.println("获得校验码：" + MAC);
                                    log += "生成校验码:" + MAC + "\n";
                                    //生成合成令牌
                                    String Token = R + "," + Long.toString(ct) + "," + preleo.getSsid().toString() + "," + MAC;
                                    System.out.println("获得认证令牌:" + Token);
                                    log += "生成令牌:" + Token + "\n";
                                    //计算CK会话密钥,注意因为加密原因必须8位，CK取前8位置
                                    String C_K = null;
                                    try {
                                        C_K = ds.DESencode(R, preleo.getMainKey());
                                    } catch (Exception e) {
                                    }
                                    System.out.println("Mainkey:" + preleo.getMainKey());
                                    String CK = null;
                                    if (C_K != null && !C_K.equals("")) {
                                        if (C_K.length() >= 8) {
                                            CK = C_K.substring(0, 8);
                                        } else {
                                            CK = C_K + 'a' * (8 - C_K.length());
                                        }
                                        System.out.println("获得会话密钥：" + CK);
                                        log += "获得会话密钥：" + CK + "\n";
                                        //计算XRES 预期响应数据
                                        String XRES = null;
                                        String newmsg = null;
                                        try {
                                            XRES = ds.DESencode(R, CK);
                                            newmsg = R + "," + TID_Src_ESrc + "," + ds.DESencode(Token, CK);
                                            System.out.println("获得预期响应数据：" + XRES);
                                            log += "生成预期响应数据" + XRES + "\n";

                                        } catch (Exception e) {

                                        }
                                        if (XRES != null && !XRES.equals("")) {
                                            try {
                                                writer.write(newmsg);
                                                writer.write("eof\n");
                                                writer.flush();
                                            } catch (IOException e) {

                                            }

                                            System.out.println("卫星" + SrcIDsat + "向卫星" + DstIDsat + "发送信息:" + newmsg);
                                            log += "卫星" + SrcIDsat + "向卫星" + DstIDsat + "发送信息:" + newmsg + "\n";
                                            //设置超时间为10秒
                                            try {
                                                client.setSoTimeout(10 * 1000);
                                            } catch (SocketException e) {
                                            }

                                            log += "Step4\n";
                                            log += "卫星" + DstIDsat + "校验数据\n";
                                            log += "卫星" + DstIDsat + "计算响应数据\n";
                                            sb.setLength(0);
                                            //最后一步 接收信息
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

                                            String RES = sb.toString();

                                            if (RES != null && !RES.equals("")) {
                                                log += "卫星" + DstIDsat + "发送信息:" + sb.toString() + "\n";
                                                System.out.println("Step5:");
                                                log += "Step5:\n";
                                                System.out.println("LEO-B接收LEO-A信息：" + RES);
                                                log += "卫星" + SrcIDsat + "接收信息:" + RES + "\n";
                                                System.out.println("LEO-B校验数据");
                                                log += "卫星" + SrcIDsat + "校验数据\n";

                                                if (RES.equals(XRES)) {
                                                    System.out.println("三方认证成功");
                                                    log += "认证成功\n";
                                                    //         iptablesManager.acceptIcmp(clientIP);//认证成功
                                                    //三方认证成功可以插入数据了
                                                    String ID_Dst = ID_Dst_ESrc;
                                                    int ssid_Dst = Integer.valueOf(SSID_Dst);
                                                    try {
                                                        sql = "insert into leoleo values(?,?,?,?,?,?,?) ";
                                                        PreparedStatement pst = connection.prepareStatement(sql);//用来执行SQL语句查询，对sql语句进行预编译处理
                                                        pst.setString(1, ID_Dst);
                                                        pst.setInt(2, ssid_Dst);
                                                        pst.setString(3, TID_Src_ESrc);
                                                        pst.setString(4, TID_Dst_ESrc);
                                                        pst.setInt(5, 1);
                                                        pst.setString(6, Token);
                                                        pst.setString(7, log);
                                                        pst.executeUpdate();//解释在下
                                                        pst.close();
                                                    } catch (SQLException e) {
                                                        e.printStackTrace();
                                                    }


                                                    //告诉目的卫星三方认证成功了可以插入数据了

                                                    String Res = "YES";
                                                    try {
                                                        writer.write(Res);
                                                        writer.write("eof\n");
                                                        writer.flush();
                                                        writer.close();
                                                        reader.close();
                                                    } catch (IOException e) {
                                                    }

                                                } else {
                                                    log += "响应数据和预置响应数据不相等！\n";
                                                    log += "认证失败\n";
                                                    // iptablesManager.DacceptIcmp(clientIP);//封禁ip
                                                    try {
                                                        sql = "insert into leoleo(IDsat,st,log) values(?,?,?)";
                                                        PreparedStatement pst = connection.prepareStatement(sql);//用来执行SQL语句查询，对sql语句进行预编译处理
                                                        pst.setString(1, DstIDsat);
                                                        pst.setInt(2, 0);
                                                        pst.setString(3, log);
                                                        pst.executeUpdate();
                                                        pst.close();

                                                    } catch (SQLException ee) {
                                                        ee.printStackTrace();
                                                    }
                                                    try {
                                                        reader.close();
                                                        writer.close();
                                                        statement.close();
                                                        connection.close();
                                                        client.close();
                                                    } catch (IOException e) {
                                                    }
                                                }

                                                try {
                                                    reader.close();
                                                    reader.close();
                                                    writer.close();
                                                    writer.close();
                                                    statement.close();
                                                    connection.close();
                                                    client.close();
                                                } catch (IOException e) {

                                                }


                                            } else {
                                                log += "卫星" + SrcIDsat + "未接收到数据\n";
                                                log += "认证失败\n";
                                                // iptablesManager.DacceptIcmp(clientIP);//封禁ip
                                                try {
                                                    sql = "insert into leoleo(IDsat,st,log) values(?,?,?)";
                                                    PreparedStatement pst = connection.prepareStatement(sql);//用来执行SQL语句查询，对sql语句进行预编译处理
                                                    pst.setString(1, DstIDsat);
                                                    pst.setInt(2, 0);
                                                    pst.setString(3, log);
                                                    pst.executeUpdate();
                                                    pst.close();

                                                } catch (SQLException ee) {
                                                    ee.printStackTrace();
                                                }
                                                try {
                                                    reader.close();
                                                    writer.close();
                                                    statement.close();
                                                    connection.close();
                                                    client.close();
                                                } catch (IOException e) {
                                                }

                                            }
                                        } else {
                                            log += "主密钥错误\n";
                                            log += "认证失败\n";
                                            // iptablesManager.DacceptIcmp(clientIP);//封禁ip
                                            try {
                                                sql = "insert into leoleo(IDsat,st,log) values(?,?,?)";
                                                PreparedStatement pst = connection.prepareStatement(sql);//用来执行SQL语句查询，对sql语句进行预编译处理
                                                pst.setString(1, DstIDsat);
                                                pst.setInt(2, 0);
                                                pst.setString(3, log);
                                                pst.executeUpdate();
                                                pst.close();

                                            } catch (SQLException ee) {
                                                ee.printStackTrace();
                                            }
                                            try {
                                                reader.close();
                                                writer.close();
                                                statement.close();
                                                connection.close();
                                                client.close();
                                            } catch (IOException e) {
                                            }
                                        }
                                    } else {
                                        log += "密钥错误\n";
                                        log += "认证失败\n";
                                        // iptablesManager.DacceptIcmp(clientIP);//封禁ip
                                        try {
                                            sql = "insert into leoleo(IDsat,st,log) values(?,?,?)";
                                            PreparedStatement pst = connection.prepareStatement(sql);//用来执行SQL语句查询，对sql语句进行预编译处理
                                            pst.setString(1, DstIDsat);
                                            pst.setInt(2, 0);
                                            pst.setString(3, log);
                                            pst.executeUpdate();
                                            pst.close();


                                        } catch (SQLException ee) {
                                            ee.printStackTrace();
                                        }
                                        try {
                                            reader.close();
                                            writer.close();
                                            statement.close();
                                            connection.close();
                                            client.close();
                                        } catch (IOException e) {
                                        }
                                    }
                                } else {
                                    log += "卫星" + SrcIDsat + "主密钥错误\n";
                                    log += "认证失败\n";
                                    // iptablesManager.DacceptIcmp(clientIP);//封禁ip
                                    try {
                                        sql = "insert into leoleo(IDsat,st,log) values(?,?,?)";
                                        PreparedStatement pst = connection.prepareStatement(sql);//用来执行SQL语句查询，对sql语句进行预编译处理
                                        pst.setString(1, DstIDsat);
                                        pst.setInt(2, 0);
                                        pst.setString(3, log);
                                        pst.executeUpdate();
                                        pst.close();

                                    } catch (SQLException ee) {
                                        ee.printStackTrace();
                                    }
                                    try {
                                        reader.close();
                                        writer.close();
                                        statement.close();
                                        connection.close();
                                        client.close();
                                    } catch (IOException e) {
                                    }
                                }
                            } else {
                                log += "校验失败\n";
                                log += "认证失败\n";
                                // iptablesManager.DacceptIcmp(clientIP);//封禁ip
                                try {
                                    sql = "insert into leoleo(IDsat,st,log) values(?,?,?)";
                                    PreparedStatement pst = connection.prepareStatement(sql);//用来执行SQL语句查询，对sql语句进行预编译处理
                                    pst.setString(1, DstIDsat);
                                    pst.setInt(2, 0);
                                    pst.setString(3, log);
                                    pst.executeUpdate();
                                    pst.close();

                                } catch (SQLException ee) {
                                    ee.printStackTrace();
                                }
                                try {
                                    reader.close();
                                    writer.close();
                                    statement.close();
                                    connection.close();
                                    client.close();
                                } catch (IOException e) {
                                }
                            }
                        }
                    } else {
                        log += "密钥错误\n";
                        log += "认证失败\n";
                        // iptablesManager.DacceptIcmp(clientIP);//封禁ip
                        try {
                            sql = "insert into leoleo(IDsat,st,log) values(?,?,?)";
                            PreparedStatement pst = connection.prepareStatement(sql);//用来执行SQL语句查询，对sql语句进行预编译处理
                            pst.setString(1, DstIDsat);
                            pst.setInt(2, 0);
                            pst.setString(3, log);
                            pst.executeUpdate();
                            pst.close();
                        } catch (SQLException ee) {
                            ee.printStackTrace();
                        }
                        try {
                            reader.close();
                            writer.close();
                            statement.close();
                            connection.close();
                            client.close();
                        } catch (IOException e) {
                        }
                    }
                }
            } else if (s.split(",")[0].equals("2")) {

                log += "两方认证\n";
                log += "卫星" + DstIDsat + "发送信息:" + s + "\n";
                //二方认证 Step2 获得来自A的数据，根据时间戳，A临时身份，Token，比较，并将B自身数据发送给A
                System.out.println("Step3:");
                log += "Step3:\n";
                System.out.println("LEO-B接收LEO-A信息：" + s);
                log += "卫星" + SrcIDsat + "接收信息:" + s + "\n";
                String T = s.split(",")[1];
                String ID_Dst = s.split(",")[2];  // 目的卫星的ID
                String TID_Dst = s.split(",")[3] + "," + s.split(",")[4]; // 目的卫星的临时身份
                String Token = s.split(",")[5] + "," + s.split(",")[6] + "," + s.split(",")[7] + "," + s.split(",")[8]; //Token

                // 查找自身认证表中的存的Idsat为A的那一行临时身份A和Token，以及临时身份B
                sql = "select * from leoleo where IDsat = '" + ID_Dst + "'";
                resultSet = statement.executeQuery(sql);
                while (resultSet.next()) {
                    leoleo.setIDsat(resultSet.getString(1));
                    leoleo.setSsid(resultSet.getInt(2));
                    leoleo.setTidSrc(resultSet.getString(3));
                    leoleo.setTidDst(resultSet.getString(4));
                    leoleo.setSt(resultSet.getInt(5));
                    leoleo.setToken(resultSet.getString(6));
                }

                resultSet.close();
                //System.out.println(leoleo.getTida());

                //比较时间戳和临时身份和Token，符合条件继续认证，不然认证失败
                Long t = Long.parseLong(T);
                //当前时间
                Long ct = System.currentTimeMillis();
                //(ct-t)<2000 &&
                if (leoleo.getToken() != null&&!leoleo.getToken().equals("")) {
                    if (TID_Dst.equals(leoleo.getTidDst()) && Token.equals(leoleo.getToken())) {
                        String newmsg_Src = leoleo.getTidSrc();
                        System.out.println("LEO-B校验数据");
                        try {
                            writer.write(newmsg_Src);
                            writer.write("eof\n");
                            writer.flush();
                        } catch (Exception e) {
                        }
                        log += "卫星" + SrcIDsat + "校验数据\n";
                        System.out.println("LEO-B向LEO-A发送信息：" + newmsg_Src);
                        log += "卫星" + SrcIDsat + "发送数据:" + newmsg_Src + "\n";


                        sb.setLength(0);
                        //设置超时间为10秒
                        try {
                            client.setSoTimeout(10 * 1000);
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

                        // 三方认证 Step 6  比较RES和XRES若一致认证成功，
                        String Res = sb.toString();
                        if (Res != null && !Res.equals("")) {
                            if (Res.equals("YES")) {
                                log += "卫星" + DstIDsat + "校验数据\n";
                                log += "认证成功\n";
                                //改自身的数据
                                System.out.println("两方认证成功");
                                try {
                                    //查询语句
                                    sql = "UPDATE leoleo set ST = 1 , log = '" + log + "'" + "WHERE IDsat = '" + ID_Dst + "'";
                                    int executeleo = statement.executeUpdate(sql);
//                                    iptablesManager.acceptIcmp(clientIP);//封禁ip
                                    statement.close();
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }

                            } else {
                                log += "临时身份或Token校验不通过\n";
                                log += "认证失败\n";
                                // iptablesManager.DacceptIcmp(clientIP);//封禁ip
                                try {
                                    sql = "delete from leoleo where IDsat='"+DstIDsat+"'";
                                    statement.execute(sql);
                                    sql = "insert into leoleo(IDsat,st,log) values(?,?,?)";
                                    PreparedStatement pst = connection.prepareStatement(sql);//用来执行SQL语句查询，对sql语句进行预编译处理
                                    pst.setString(1, DstIDsat);
                                    pst.setInt(2, 0);
                                    pst.setString(3, log);
                                    pst.executeUpdate();
                                    pst.close();

                                } catch (SQLException ee) {
                                    ee.printStackTrace();
                                }
                            }
                            statement.close();
                            connection.close();
                            try {
                                client.close();
                            } catch (IOException e) {

                            }
                        } else {
                            log += "卫星" + DstIDsat + "校验数据\n";
                            log += "校验不通过\n";
                            log += "认证失败\n";
                            // iptablesManager.DacceptIcmp(clientIP);//封禁ip
                            try {
                                sql = "delete from leoleo where IDsat='"+DstIDsat+"'";
                                statement.execute(sql);
                                sql = "insert into leoleo(IDsat,st,log) values(?,?,?)";
                                PreparedStatement pst = connection.prepareStatement(sql);//用来执行SQL语句查询，对sql语句进行预编译处理
                                pst.setString(1, DstIDsat);
                                pst.setInt(2, 0);
                                pst.setString(3, log);
                                pst.executeUpdate();
                                pst.close();

                            } catch (SQLException ee) {
                                ee.printStackTrace();
                            }
                        }
                    } else {
                        log += "临时身份或Token校验不通过\n";
                        log += "认证失败\n";
                        try {
                            writer.write("临时身份或Token校验不通过");
                            writer.write("eof\n");
                            writer.flush();
                        } catch (Exception e) {
                        }
                        // iptablesManager.DacceptIcmp(clientIP);//封禁ip
                        try {
                            sql = "delete from leoleo where IDsat='"+DstIDsat+"'";
                            statement.execute(sql);
                            sql = "insert into leoleo(IDsat,st,log) values(?,?,?)";
                            PreparedStatement pst = connection.prepareStatement(sql);//用来执行SQL语句查询，对sql语句进行预编译处理
                            pst.setString(1, DstIDsat);
                            pst.setInt(2, 0);
                            pst.setString(3, log);
                            pst.executeUpdate();
                            pst.close();

                        } catch (SQLException ee) {
                            ee.printStackTrace();
                        }
                    }
                } else {
                    log += "Token为空！\n";
                    log += "认证失败\n";
                    try {
                        writer.write("Token为空");
                        writer.write("eof\n");
                        writer.flush();
                    } catch (Exception e) {
                    }
                    //   iptablesManager.DacceptIcmp(clientIP);//封禁ip
                    try {
                        sql = "delete from leoleo where IDsat='"+DstIDsat+"'";
                        statement.execute(sql);
                        sql = "insert into leoleo(IDsat,st,log) values(?,?,?)";
                        PreparedStatement pst = connection.prepareStatement(sql);//用来执行SQL语句查询，对sql语句进行预编译处理
                        pst.setString(1, DstIDsat);
                        pst.setInt(2, 0);
                        pst.setString(3, log);
                        pst.executeUpdate();
                        pst.close();
                    } catch (SQLException ee) {
                        ee.printStackTrace();
                    }
                }
            }

        }
    }
}


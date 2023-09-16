package com.sat.utils;

import com.sat.satquery.entity.Preleo;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;

//星地认证三方认证过程中星地通信类
public class TccLeoLeoAuthTask implements Runnable {

    private Socket client;

    private Preleo preleo;

    //星地认证发起方的广播编号
    Integer SSID;

    private String a= "";

    public TccLeoLeoAuthTask(Socket client, Preleo preleo, Integer SSID) {
        this.client = client;
        this.preleo = preleo;
        this.SSID = SSID;
    }

    public void run() {
        try {
            a = handleSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    public String getData(){
        return a;
    }

    /**
     * 跟客户端Socket进行通信
     * @throws Exception
     */
    private String handleSocket() throws SQLException, ClassNotFoundException {
        String log = "";
        //  注释是是查jdbc连接数据库查询数据表
        String url = "jdbc:sqlite::resource:db/leoa.db";
        Class.forName("org.sqlite.JDBC");
        Connection connection = DriverManager.getConnection(url);
        Statement statement = connection.createStatement();
        System.out.println("本卫星预置表信息："+preleo);
        DESUtils ds = new DESUtils();
        // 三方认证Step1 向地面发送带标记的和自身Id, SSID_B和时间戳并且经过加密的消息
        //发送的消息 标记为1  后面tcc处理的时候根据1判断是星地认证 还是星间认证

        // 获得时间戳 13位数的
        Long l = System.currentTimeMillis();
        String t = Long.toString(l);
        // 加密发送的消息

        //目的卫星和地面通信发送的信息 包括  自身的编号 ，源卫星的广播ID
        String msg = preleo.getIDsat()+","+SSID.toString() +","+ t;
        try {
            msg = ds.DESencode(msg,preleo.getK());
        } catch (Exception e) {
        }
        if(msg!= null && !msg.equals("")) {   ///密钥错误 会继续执行 但是 msg为空
            System.out.println("加密信息:" + msg);
            msg = "1," + msg;
            System.out.println("星地认证向TCC发送信息:" + msg);
            //向Tcc发送消息
            Writer writer = null;
            log += "卫星" + preleo.getIDsat() + "向地面发送信息:" + msg + "\n";
            try {
                writer = new OutputStreamWriter(client.getOutputStream(), "GBK");
                writer.write(msg);
                writer.write("eof\n");
                writer.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }

            //System.out.println("发送信息：");


            //三方认证 Step 3 接收地面发来的消息对前半部分进行解密，获得需要的信息，验证时间戳 合成新请求发送给B
            // Step3
            //读取发送的信息
            BufferedReader br = null;

            //设置超时间为10秒

            //client.setSoTimeout(10*1000);
            StringBuffer sb = new StringBuffer();
            String temp;
            int index;
            //设置超时间为10秒
            try {
                client.setSoTimeout(10 * 1000);
            } catch (SocketException e) {
                System.out.println("超时");
                e.printStackTrace();
            }
            sb.setLength(0);
            try {
                br = new BufferedReader(new InputStreamReader(client.getInputStream(), "UTF-8"));
                while ((temp = br.readLine()) != null) {
                    if ((index = temp.indexOf("eof")) != -1) {
                        sb.append(temp.substring(0, index));
                        break;
                    }
                    sb.append(temp);
                }
            } catch (IOException e) {
            }
            String s = sb.toString();
            if (s != null && s.equals("")) {
                log += "卫星" + preleo.getIDsat() + "密钥K错误" + "\n";
                try {
                    writer.close();
                    br.close();
                    client.close();
                } catch (IOException ex) {

                }
                System.out.println(log);
                return log;
            } else {
                //System.out.println("三方第三步，A获取需要解密信息");
                //System.out.println(s);
                try {
                    writer.close();
                    br.close();
                    client.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                //返回和地面通信获得的信息和流程，逗号分隔
                return s + "," + log;
            }
        }else {
            return "目的卫星密钥K错误\n";
        }
    }

}


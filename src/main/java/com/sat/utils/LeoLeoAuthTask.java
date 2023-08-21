package com.sat.utils;

import com.sat.satquery.entity.Preleo;
import com.sat.satquery.entity.Tccleo;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;


public class LeoLeoAuthTask implements Runnable {

    private Socket client;

    private Preleo preleo;

    Integer SSID_B;

    private String a= "";

    public LeoLeoAuthTask(Socket client, Preleo preleo, Integer SSID_B) {
        this.client = client;
        this.preleo = preleo;
        this.SSID_B = SSID_B;
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
    private String handleSocket() throws Exception {
        //进入子线程

        //  注释是是查jdbc连接数据库查询数据表
        String url = "jdbc:mysql://localhost:3306/leoa";
        Connection connection = DriverManager.getConnection(url,"root","123456");
        Statement statement = connection.createStatement();
        System.out.println("本卫星预置表信息："+preleo);
        DESUtils ds = new DESUtils();
        // 三方认证Step1 向地面发送带标记的和自身Id, SSID_B和时间戳并且经过加密的消息
        //发送的消息 标记为1  后面tcc处理的时候根据1判断是星地认证 还是星间认证

        // 获得时间戳 13位数的
        Long l = System.currentTimeMillis();
        String t = Long.toString(l);
        // 加密发送的消息

//        //(1)、如果数字1是字符串,如下处理：
//        String str1="1";
//        DecimalFormat df=new DecimalFormat("0000");
//        String str2=df.format(Integer.parseInt(str1));
//        System.out.println(str2);
//       //(2)、如果数字1是整型,如下处理：
//        int str1=1;
//        DecimalFormat df=new DecimalFormat("0000");
//        String str2=df.format(str1);
//        System.out.println(str2);
//        String msg=ds.DESencode("1,"+preleo.getIDsat()+SSID_B+t,preleo.getK());

        //加密，将SSID和IDsat补齐四位，便于加密，DES加密要求数据必须是8的倍数
        DecimalFormat df=new DecimalFormat("0000");
        String str1 = df.format(preleo.getIDsat());
        String str2 = df.format(Integer.valueOf(SSID_B));
        //System.out.println(str1);
        //System.out.println(str2);
        String msg_A = "0"+ str1+","+str2 +","+ t;
        String msgA = ds.DESencode(msg_A,preleo.getK());
        System.out.println("LEO-A加密信息:"+msgA);
        String msg ="1," + msgA ;

        System.out.println("LEO-A向TCC发送信息:"+msgA);

        //向Tcc发送消息
        Writer writer = new OutputStreamWriter(client.getOutputStream(), "GBK");
        writer.write(msg);
        writer.write("eof\n");
        writer.flush();
        //System.out.println("发送信息：");


        //三方认证 Step 3 接收地面发来的消息对前半部分进行解密，获得需要的信息，验证时间戳 合成新请求发送给B
        // Step3
        //读取发送的信息
        BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream(), "UTF-8"));
        //设置超时间为10秒

        //client.setSoTimeout(10*1000);
        StringBuffer sb = new StringBuffer();
        String temp;
        int index;
        //设置超时间为10秒
        try {
            client.setSoTimeout(10 * 1000);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        try {
            while ((temp=br.readLine()) != null) {
                if ((index = temp.indexOf("eof")) != -1) {
                    sb.append(temp.substring(0, index));
                    break;
                }
                sb.append(temp);
            }
        } catch (SocketTimeoutException e) {
            System.out.println("数据读取超时。");
        }
        String s = sb.toString();
        //System.out.println("三方第三步，A获取需要解密信息");
        //System.out.println(s);

        writer.close();
        br.close();
        client.close();
        return s;

    }

}


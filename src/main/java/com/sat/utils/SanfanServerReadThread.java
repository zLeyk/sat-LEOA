package com.sat.utils;

import com.sat.satquery.entity.Preleo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;


import java.io.*;
import java.net.*;

public class SanfanServerReadThread implements Runnable {
    private Socket socket;
    private Preleo preleo;
    String TID_B;
    private String a= "";

    public SanfanServerReadThread(Socket socket, Preleo preleo,String TID_B) {
        this.socket = socket;
        this.TID_B = TID_B;
        this.preleo = preleo;
    }

    public void run() {
        try {
            a = handleSocket();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String getData(){
        return a;
    }

    private String  handleSocket() throws Exception {
        DESUtils ds = new DESUtils();
        System.out.println("来到三方认证子线程来完成第四五步");
        //在子线程继续而三方认证第四步，接受来自B的数据

        BufferedReader br = null;
        br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        //读消息的代码来自B的消息
        StringBuilder sb = new StringBuilder();
        String temp;
        int index;
        while ((temp=br.readLine()) != null) {
            if ((index = temp.indexOf("eof")) != -1) {
                sb.append(temp.substring(0, index));
                break;
            }
            sb.append(temp);
        }


        String msgb = sb.toString();
        System.out.println("三方认证第四步，获得传来的数据");
        System.out.println(msgb);
        String R = msgb.split(",")[0];
        String TID_B2 = msgb.split(",")[1] + "," + msgb.split(",")[2];

        //计算CK会话密钥,注意因为加密原因必须8位，CK取前8位置,和B中过程一样的目的就是得到和B一样的CK
        String C_K = ds.DESencode(R,preleo.getMainKey());
        System.out.println(C_K);
        String CK = C_K.substring(0, 8);
        System.out.println(CK);

        //利用CK进行解密Token获得时间戳和校验码，比较时间戳和校验码是否一致
        String Token = ds.DESdecode(msgb.split(",")[3], CK);
        System.out.println(Token);
        String Tt = Token.split(",")[1];
        String MAC = Token.split(",")[3];

        //计算MAC
        String XMAC = ds.DESencode(R + Tt + TID_B, preleo.getMainKey());
        System.out.println(XMAC);
        long Ttt = System.currentTimeMillis();
        if ((Ttt - Long.parseLong(Tt) < 2000) && MAC.equals(XMAC) ) {

            //计算XRES 预期响应数据
            System.out.println("三方认证第四步，校验完毕数据符合要求");
            String XRES = ds.DESencode(R, CK);
            Writer writer = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
            writer.write(XRES);
            writer.write("eof\n");
            writer.flush();

            System.out.println("三方认证第五步,A向B发送数据成功");

            writer.close();
            br.close();
            socket.close();
            return Token;


            //将B的信息存入星星认证表
            //String sql1 =  "insert into leoleo(IDsat,SSID,lida,lidb,St,Token) VALUES(ID_B,SSID_B,TID_A,TID_B,'1',Token)";

        }else {
            System.out.println("三方认证失败");

            br.close();
            socket.close();
            return "0";

        }



    }
}

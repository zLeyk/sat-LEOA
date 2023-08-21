package com.sat.utils;

import java.io.*;
import java.net.*;

public class LiangfanServerReadThread implements Runnable {
    private Socket socket;
    String msg;
    private String a= "";;

    public LiangfanServerReadThread(Socket socket, String msg) {
        this.socket = socket;
        this.msg = msg;
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

    private String handleSocket() throws Exception {
        //System.out.println("来到两方认证子线程完成剩余两方认证操作");
        //在子线程继续而二方认证第三步
        BufferedReader br = null;
        br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        //读消息的代码来自B的消息
        StringBuilder sb = new StringBuilder();
        String temp;
        int index;
        //设置超时间为10秒
        try {
            socket.setSoTimeout(10 * 1000);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        while ((temp = br.readLine()) != null) {
            if ((index = temp.indexOf("eof")) != -1) {
                sb.append(temp.substring(0, index));
                break;
            }
            sb.append(temp);
        }
        String newTID_B = sb.toString();
        System.out.println("LEO-A接收LEO-B信息："+newTID_B);
        if (newTID_B.equals(msg)) {
            System.out.println("LEO-A校验数据");
            System.out.println("二次认证成功");
            br.close();
            socket.close();
            return "1";

        } else {
            System.out.println("LEO-A校验数据");
            System.out.println("二次认证失败");
            br.close();
            socket.close();
            return "0";
        }

    }



}
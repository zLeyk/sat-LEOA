package com.sat.utils;

import java.io.*;
import java.net.*;

public class LiangfanServerReadThread implements Runnable {
    private Socket socket;
    String msg;
    public LiangfanServerReadThread(Socket socket, String msg) {
        this.socket = socket;
        this.msg = msg;
    }
    public void run() {
        try {
            handleSocket();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    private void handleSocket() throws Exception {
        System.out.println("来到两方认证子线程完成剩余两方认证操作");
        //在子线程继续而二方认证第三步
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
        String newTID_B = sb.toString();
        System.out.println("二方认证第三步，接收B向A发送数据成功");
        if (newTID_B.equals(msg)){
            System.out.println("二方认证第三步，整体认证成功");

        }else {System.out.println("二方认证第三步，整体认证失败");}
        br.close();
        socket.close();


    }
}
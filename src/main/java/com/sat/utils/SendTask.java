package com.sat.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.SocketTimeoutException;

//用于测试的时候发送消息。实际开发中，发送几种认证请求就创建几个任务类，命名为 消息类型Task。
public class SendTask implements Runnable {

    private Socket client;

    public SendTask(Socket client) {
        this.client = client;
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

        //建立连接后就可以往服务端写数据了
        Writer writer = new OutputStreamWriter(client.getOutputStream(), "GBK");
        writer.write("你好，"+client.getPort());
        writer.write("eof\n");
        writer.flush();
        //写完以后进行读操作
        BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream(), "UTF-8"));
        //设置超时间为10秒
        client.setSoTimeout(10*1000);
        StringBuffer sb = new StringBuffer();
        String temp;
        int index;
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
        System.out.println(" " + sb);
        writer.close();
        br.close();
        client.close();
    }
}


package com.sat;

import com.sat.utils.LEOTask;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

@SpringBootApplication
@MapperScan("com.sat.satquery.mapper")
@MapperScan("com.sat.dao") // 添加包扫描后解决报错问题
public class SatApplication {
    String IP = "192.168.10.23";
    public static void main(String[] args) throws IOException {
        SpringApplication.run(SatApplication.class, args);


        //为了简单起见，所有的异常信息都往外抛
        int port = 8897;
        //定义一个ServerSocket监听在端口8897上
        ServerSocket server = new ServerSocket(port);
        while (true) {
            //server尝试接收其他Socket的连接请求，server的accept方法是阻塞式的
            Socket socket = server.accept();
            //每接收到一个Socket就建立一个新的线程来处理它
            new Thread(new LEOTask(socket)).start();
        }
    }
    /**
     * 用来处理Socket请求的
     */
    static class Task implements Runnable {

        private Socket socket;

        public Task(Socket socket) {
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
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "GBK"));
            StringBuilder sb = new StringBuilder();
            String temp;
            int index;
            InetAddress InIP = socket.getLocalAddress();
            System.out.println("消息来自"+InIP+" "+socket.getPort());
            while ((temp=br.readLine()) != null) {
                //System.out.println(temp);
                if ((index = temp.indexOf("eof")) != -1) {//遇到eof时就结束接收
                    sb.append(temp.substring(0, index));
                    break;
                }
                sb.append(temp);
            }
            System.out.println(socket.getInetAddress()+":"+socket.getPort()+"说  " + sb);
            //读完后写一句
            Writer writer = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
            writer.write("我是LEO A：你好 "+"");
            writer.write("eof\n");
            writer.flush();
            writer.close();
            br.close();
            socket.close();
        }
    }


}



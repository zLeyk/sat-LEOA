package com.sat.utils;

import com.sat.satquery.entity.Preleo;
import com.sat.satquery.service.IPreleoService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.List;

//星地认证流程
public class LeoTccAuthTask implements Runnable {

    private Socket client;
    private String st;


    private Preleo preleo;
    public LeoTccAuthTask(Socket client, Preleo preleo) {
        this.client = client;
        this.preleo = preleo;
    }

    public String getSt() {
        return st;
    }

    public void setSt(String st) {
        this.st = st;
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
        setSt("认证失败");
        System.out.println(preleo);
        BufferedWriter wr = new BufferedWriter(new FileWriter("au.txt"));
        DESUtils ds = new DESUtils();
        //发送的消息  消息类型
        String msg = "0,";
        //Step1
        //获得时间戳
        Long l = System.currentTimeMillis();
        String t = Long.toString(l);
        //临时身份
        String iDsat = preleo.getIDsat().toString();
        //临时身份
        String Tid = t + "," + iDsat;
        msg = msg + ds.DESencode(Tid, preleo.getK());
        System.out.println("Step1:");
        wr.write("Step1:\n");
        System.out.println("临时身份:" + Tid);
        wr.write("临时身份:" + Tid);
        wr.write("\n");
        System.out.println("LEO发送信息:" + msg);
        wr.write("LEO发送信息:" + msg+"\n");

        Writer writer = null;
        BufferedReader br = null;
        writer = new OutputStreamWriter(client.getOutputStream(), "GBK");
        writer.write(msg);
        writer.write("eof\n");
        writer.flush();
        br = new BufferedReader(new InputStreamReader(client.getInputStream(), "UTF-8"));

        Thread.sleep(2000);
        // Step3
        //读取发送的信息

        //设置超时间为10秒
        try {
            client.setSoTimeout(10 * 1000);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        StringBuffer sb = new StringBuffer();
        String temp;
        int index;
        while ((temp = br.readLine()) != null) {
            if ((index = temp.indexOf("eof")) != -1) {
                sb.append(temp.substring(0, index));
                break;
            }
            sb.append(temp);
        }
        String s = sb.toString();
        wr.write("Step2:\n");
        wr.write("TCC验证临时身份\n");
        wr.write("TCC合成认证数据\n");
        wr.write("TCC发送信息:"+s+"\n");
        System.out.println("Step3:");
        wr.write("Step3:\n");
        //合成CK
        String CK;
        String cks = new StringBuffer(MD5Utils.encrypt(preleo.getDkauth()+preleo.getDkenc())).reverse().toString();

        if (cks.length()>=8){
            CK = cks.substring(0,8);
        }else {
            CK = cks+'a'*(8-cks.length());
        }


        //解密数据
        System.out.println("接收信息:"+s);
        wr.write("LEO接收信息:"+s+"\n");
        try {
            s = ds.DESdecode(s, CK);
            System.out.println("解密信息:" + s);
            wr.write("解密信息:" + s+"\n");
            if (s.split(",").length != 3) {
                System.out.println("认证失败");
                wr.write("认证失败\n");
                wr.close();
                writer.close();
                br.close();
                client.close();
            } else {
                //分解获得的数据
                //时间
                String Tre = s.split(",")[0];
                //Mac
                String MAC = s.split(",")[1];
                //随机数R
                String R = s.split(",")[2];

                //验证
                long ct = System.currentTimeMillis();  //当前时间
                //计算XMAC
                String XMAC = ds.DESencode(R+Tre, preleo.getWKenc());
                if ((ct - Long.parseLong(Tre)) > 2000 || !(XMAC.equals(MAC))) {
                    System.out.println("校验不通过");
                    wr.write("校验不通过\n");
                    writer.close();
                    br.close();
                    client.close();
                } else {
                    String Res = ds.DESencode(R, CK);
                    String req = ct + "," + Res;
                    req = ds.DESencode(req, CK);
                    System.out.println("发送信息:" + req);
                    wr.write("发送信息:" + req);
                    wr.write("\n");
                    writer.write(req);
                    writer.write("eof\n");
                    writer.flush();
                    Thread.sleep(2000);
                    //接受信息
                    sb.setLength(0);
                    while ((temp = br.readLine()) != null) {
                        if ((index = temp.indexOf("eof")) != -1) {
                            sb.append(temp.substring(0, index));
                            break;
                        }
                        sb.append(temp);
                    }
                    wr.write("Step4:\n");
                    wr.write("TCC校验数据\n");
                    if (sb.toString().equals("认证成功")) {
                        System.out.println("认证成功");
                        wr.write("认证成功\n");
                        setSt("认证成功");
                    }
                    wr.close();
                    writer.close();
                    br.close();
                    client.close();
                }
            }
        }catch (Exception e){
            System.out.println("认证失败");
            wr.write("认证失败\n");
            wr.close();
        }

    }
}


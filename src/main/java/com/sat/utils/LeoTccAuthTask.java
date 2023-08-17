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
        System.out.println("临时身份:" + Tid);
        System.out.println("步骤1发送的信息:" + msg);
        Writer writer = null;
        BufferedReader br = null;
        writer = new OutputStreamWriter(client.getOutputStream(), "GBK");
        writer.write(msg);
        writer.write("eof\n");
        writer.flush();
        br = new BufferedReader(new InputStreamReader(client.getInputStream(), "UTF-8"));

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
        System.out.println("Step3:");
        //合成CK
        String CK = preleo.getDkauth().substring(0, 4) + preleo.getDkenc().substring(0, 4);
        System.out.println("CK:" + CK);
        //解密数据
        System.out.println("解密Ck:"+CK);
        System.out.println("加密认证数据s:"+s);
        try {
            s = ds.DESdecode(s, CK);
            System.out.println("解密收到的认证数据:" + s);

            if (s.split(",").length != 3) {
                System.out.println("认证失败");
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
                    writer.close();
                    br.close();
                    client.close();
                } else {
                    String Res = ds.DESencode(R, CK);
                    String req = ct + "," + Res;
                    req = ds.DESencode(req, CK);
                    System.out.println("发送的信息:" + req);
                    writer.write(req);
                    writer.write("eof\n");
                    writer.flush();

                    //接受信息
                    sb.setLength(0);
                    while ((temp = br.readLine()) != null) {
                        if ((index = temp.indexOf("eof")) != -1) {
                            sb.append(temp.substring(0, index));
                            break;
                        }
                        sb.append(temp);
                    }

                    if (sb.toString().equals("认证成功")) {
                        System.out.println("认证成功");
                        setSt("认证成功");
                    }
                    writer.close();
                    br.close();
                    client.close();
                }
            }
        }catch (Exception e){
            System.out.println("认证失败");
        }

    }
}


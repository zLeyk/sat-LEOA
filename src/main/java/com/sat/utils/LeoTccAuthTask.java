package com.sat.utils;

import com.sat.satquery.entity.Preleo;

import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;

//星地认证流程
public class LeoTccAuthTask implements Runnable {

    private Socket client;
    private String st;

//    private String log;

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
    private void handleSocket(){
        setSt("认证失败");

        DESUtils ds = new DESUtils();
        MD5Utils md = new MD5Utils();
        //发送的消息  消息类型
        String msg = "0,";
        //Step1
        //获得时间戳
        Long l = System.currentTimeMillis();
        String t = Long.toString(l);
        //临时身份
        String iDsat = preleo.getIDsat();
        //临时身份
        String Tid = t + "," + iDsat;
        try {
            msg = msg + ds.DESencode(Tid, preleo.getK())+","+preleo.getIDsat();   //  密钥长度不符合 不考虑
        } catch (Exception e) {
            System.out.println("密钥错误");
            setSt("身份密钥错误");
            try {
                client.close();
            } catch (IOException ex) {

            }
        }

        Writer writer = null;
        BufferedReader br = null;
        try {
            writer = new OutputStreamWriter(client.getOutputStream(), "GBK");
            writer.write(msg);
            writer.write("eof\n");
            writer.flush();
        } catch (IOException e) {

        }


        try {
            br = new BufferedReader(new InputStreamReader(client.getInputStream(), "UTF-8"));
        } catch (IOException e) {

        }

        // Step3
        //读取发送的信息

        //设置超时间为10秒
        try {
            client.setSoTimeout(10 * 1000);
        } catch (SocketException e) {
        }
        StringBuffer sb = new StringBuffer();
        String temp;
        int index;
        try {
            while ((temp = br.readLine()) != null) {
                if ((index = temp.indexOf("eof")) != -1) {
                    sb.append(temp.substring(0, index));
                    break;
                }
                sb.append(temp);
            }
        }catch (Exception e){

        }

        String s = sb.toString();
//        wr.write("Step2:\n");
//        wr.write("TCC验证临时身份\n");
//        wr.write("TCC合成认证数据\n");
//        wr.write("TCC发送信息:"+s+"\n");
//        wr.write("Step3:\n");
//        System.out.println("Step2:\n"+"<br/>");
//        System.out.println("TCC验证临时身份\n"+"<br/>");
//        System.out.println("TCC合成认证数据\n"+"<br/>");
//        System.out.println("TCC发送信息:"+s+"\n"+"<br/>");
//        System.out.println("Step3:"+"<br/>");
//        pro += "Step2:\n";
//        pro += "TCC验证临时身份\n";
//        pro += "TCC合成认证数据\n";
//        pro += "TCC发送信息:";
//        pro += "Step3:";



        //解密数据
//        System.out.println("接收信息:"+s+"<br/>");
//        pro += "接收信息:"+s;
//        wr.write("LEO接收信息:"+s+"\n");
//        try {
        if(!s.equals("")) {  //收到了地面的信息
            //合成CK
            String CK;
            String cks = new StringBuffer(md.encrypt(preleo.getDkauth()+preleo.getDkenc())).reverse().toString();

            if (cks.length()>= 8){
                CK = cks.substring(0,8);
            }else {
                CK = cks+'a'*(8-cks.length());
            }

            try {
                s = ds.DESdecode(s, CK);
            } catch (Exception e) {
                System.out.println("解密失败");
            }
//            System.out.println("解密信息:" + s + "<br/>");
//            pro += "解密信息:" + s;
//            wr.write("解密信息:" + s + "\n");
            if (s.split(",").length != 3) {
//                System.out.println("认证失败" + "<br/>");
//                pro += "认证失败";
//                setLog(pro);
//                wr.write("认证失败\n");
//                wr.close();
                System.out.println("解密失败\n");
                try {
                    writer.close();
                    br.close();
                    client.close();
                } catch (IOException e) {

                }
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
                String XMAC = null;
                try {
                    XMAC = ds.DESencode(R + Tre, preleo.getWKenc());
                } catch (Exception e) {
                    System.out.println("解密失败，工作密钥不正确");
                }
                if (!XMAC.equals("")) {
                    if ((ct - Long.parseLong(Tre)) > 20000 || !(XMAC.equals(MAC))) {
//                    System.out.println("校验不通过" + "<br/>");
//                    pro += "认证失败";
//                    setLog(pro);
//                    wr.write("校验不通过\n");
                        System.out.println("时间不符合新鲜性要求，或者消息校验码不正确");
                        try {
                            writer.close();
                            br.close();
                            client.close();
                        } catch (IOException e) {

                        }
                    } else {
                        String Res = null;
                        try {
                            Res = ds.DESencode(R, CK);   //计算响应数据
                        } catch (Exception e) {

                        }
                        String req = ct + "," + Res;
                        try {
                            req = ds.DESencode(req, CK);  // 生成  新的请求
                        } catch (Exception e) {
                            System.out.println("会话密钥不正确");
                        }
//                    System.out.println("发送信息:" + req + "<br/>");
//                    pro += "发送信息:" + req;
//                    wr.write("发送信息:" + req);
//                    wr.write("\n");

                        try {
                            writer.write(req);
                            writer.write("eof\n");
                            writer.flush();
                        } catch (IOException e) {

                        }


                        //接受信息
//                    sb.setLength(0);


//                    wr.close();
                        try {
                            writer.close();
                            br.close();
                            client.close();
                        } catch (IOException e) {

                        }
                    }
                }
            }
        }else {
            System.out.println("没有收到地面的信息，认证失败");
        }
    }

}



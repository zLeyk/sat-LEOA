package com.sat.utils;

import com.sat.satquery.entity.Preleo;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

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
        System.out.println("卫星预置信息:"+preleo);
        //临时身份
        String Tid = t + "," + iDsat;

        System.out.println("Step1:");
        try {
            msg = msg + ds.DESencode(Tid, preleo.getK())+","+preleo.getIDsat();   //  密钥长度不符合 不考虑
        } catch (Exception e) {
            System.out.println("卫星密钥错误");
            setSt("身份密钥错误");
            try {
                client.close();
            } catch (IOException ex) {
            }
        }
        System.out.println("卫星端生成临时身份");
        System.out.println("卫星端发送信息:"+msg);
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
        System.out.println("Step2:");
        System.out.println("地面段端校验数据");
        if(!s.equals("")) {  //收到了地面的信息
            //合成CK
            String CK;
            String cks = new StringBuffer(md.encrypt(preleo.getDkauth()+preleo.getDkenc())).reverse().toString();
            if (cks.length()>= 8){
                CK = cks.substring(0,8);
            }else {
                CK = cks+'a'*(8-cks.length());
            }
            System.out.println("地面段发送信息:"+s);
            System.out.println("Step3:");
            System.out.println("卫星端接收数据");
            try {
                s = ds.DESdecode(s, CK);
            } catch (Exception e) {
            }
            if (s.split(",").length != 3) {
                System.out.println("密钥错误");
                System.out.println("认证失败");
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

                System.out.println("卫星端校验数据");
                //验证
                long ct = System.currentTimeMillis();  //当前时间
                //计算XMAC
                String XMAC = null;
                try {
                    XMAC = ds.DESencode(R + Tre, preleo.getWKenc());
                } catch (Exception e) {
                    System.out.println("加密失败，工作密钥不正确");
                    System.out.println("XMAC"+XMAC);
                }
                if (XMAC!=null && !XMAC.equals("")) {
                    if ((ct - Long.parseLong(Tre)) > 10000 || !(XMAC.equals(MAC))) {
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
                        System.out.println("卫星端发送数据"+req);
                        try {
                            writer.write(req);
                            writer.write("eof\n");
                            writer.flush();
                        } catch (IOException e) {

                        }


                        sb.setLength(0);
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
                        System.out.println("Step4:");
                        System.out.println("地面端校验数据");
                        if(sb.toString().equals("认证成功")){
                            System.out.println("认证成功");
                        } else {
                            System.out.println("认证失败");
                        }

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
            System.out.println("地面段校验数据失败");
            System.out.println("没有收到地面端的信息");
            System.out.println("认证失败");
        }
    }

}



package com.sat.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;


//DES类
public class DESUtils {
    public  String DESencode(String input, String key) throws Exception {
        String transformation = "DES";
        String algorithm = "DES";
        Cipher cipher = Cipher.getInstance(transformation);
        SecretKeySpec spec = new SecretKeySpec(key.getBytes(), algorithm);
        cipher.init(1, spec);
        byte[] bytes = cipher.doFinal(input.getBytes());
        String encode = Base64.getEncoder().encodeToString(bytes);
        return encode;
    }

    /**
     * DES解密算法
     * @param input
     * @param key
     * @return
     * @throws Exception
     */
    public  String DESdecode(String input, String key) throws Exception {
        String transformation = "DES";
        String algorithm = "DES";
        Cipher cipher = Cipher.getInstance(transformation);
        SecretKeySpec spec = new SecretKeySpec(key.getBytes(), algorithm);
        cipher.init(Cipher.DECRYPT_MODE, spec);
        byte[] decode = Base64.getDecoder().decode(input.getBytes());
        byte[] bytes = cipher.doFinal(decode);
        return new String(bytes);
    }

    public static void main(String[] args)  {
        DESUtils ds = new DESUtils();
        String msg = "hello world!";
        for(int i = 0; i <=9; i++){
            for(int j = 0; j <=9; j++){
                if(i!=j) {
                    String key1 = "ab2222" + Integer.toString(i)+ Integer.toString(i);
                    String key2 = "ab2222" + Integer.toString(j)+ Integer.toString(j);
                    String encodeMsg = null;
                    try {
                        encodeMsg = ds.DESencode(msg,key1);
                    } catch (Exception e) {

                    }
                    String decodeMsg = null;
                    try {
                        decodeMsg = ds.DESdecode(encodeMsg,key2);
                    } catch (Exception e) {

                    }
                    if(decodeMsg!=null && decodeMsg.equals(msg)){
                        System.out.println("(i,j)->("+i+","+j+")");
                    }
                }
            }
        }


    }
}

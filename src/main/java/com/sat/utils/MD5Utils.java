package com.sat.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Utils {

    public String encrypt(String key){
        char hexDigests[] = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        try {
            byte[] in = key.getBytes();
            MessageDigest messageDigest = MessageDigest.getInstance("md5");
            messageDigest.update(in);
            // 获得密文
            byte[] md = messageDigest.digest();
            // 将密文转换成16进制字符串形式
            int j = md.length;
            char[] str = new char[j*2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte b = md[i];
                str[k++] = hexDigests[b >>> 4 & 0xf];  // >>> 无符号右移。这里将字节b右移4位，低位抛弃，就等于是高4位于0xf做与运算。4位最多表示15。
                str[k++] = hexDigests[b & 0xf]; //用 1字节=8位，与0xf与运算，高4位必为0，就得到了低四位的数。
            }
            return new String(str);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException("md5加密失败",e);
        }
    }

}

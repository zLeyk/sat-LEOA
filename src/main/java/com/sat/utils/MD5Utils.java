package com.sat.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Utils {


    public static String encrypt(String input) {
        try {
            // 创建MD5摘要算法的实例
            MessageDigest md = MessageDigest.getInstance("MD5");

            // 将字符串转换为字节数组
            byte[] inputData = input.getBytes();

            // 计算MD5摘要
            byte[] encryptedData = md.digest(inputData);

            // 将字节数组转换为十六进制字符串
            StringBuilder sb = new StringBuilder();
            for (byte b : encryptedData) {
                sb.append(String.format("%02x", b));
            }

            // 返回加密后的字符串
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

}

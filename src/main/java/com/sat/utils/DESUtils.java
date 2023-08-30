package com.sat.utils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;


//DES类
public class DESUtils {
    public  String DESencode(String input, String key) throws NoSuchPaddingException, NoSuchAlgorithmException {
        String transformation = "DES";
        String algorithm = "DES";
        Cipher cipher = null;
        cipher = Cipher.getInstance(transformation);
        SecretKeySpec spec = new SecretKeySpec(key.getBytes(), algorithm);
        try {
            cipher.init(1, spec);
        } catch (InvalidKeyException e) {
            return "0";
        }
        byte[] bytes = new byte[0];
        try {
            bytes = cipher.doFinal(input.getBytes());
        } catch (IllegalBlockSizeException e) {
            return "0";
        } catch (BadPaddingException e) {
            return "0";
        }
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
}

package com.denghb.utils;


import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import java.security.Key;
import java.security.SecureRandom;

/**
 * Created by denghb on 2017/6/26.
 */
public class AESUtils {


    /***
     * 加密
     *
     * @param plain
     * @param key
     * @return 输出密文 16进制
     */
    public static String encrypt(String plain, String key) {
        try {
            byte[] byteContent = plain.getBytes("utf-8");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, getKey(key));

            byte[] buf = cipher.doFinal(byteContent);

            // byte[]转16进制
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < buf.length; i++) {
                String hex = Integer.toHexString(buf[i] & 0xFF);
                if (hex.length() == 1) {
                    hex = '0' + hex;
                }
                sb.append(hex.toUpperCase());
            }
            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /***
     * 解密 输入16进制的字符串
     *
     * @param layer
     * @param key
     * @return 原文
     */
    public static String decrypt(String layer, String key) {
        try {
            byte[] byteContent = new byte[layer.length() / 2];
            if (layer.length() < 1) {
                return null;
            }
            // 将16进制转换为二进制
            for (int i = 0; i < layer.length() / 2; i++) {
                int high = Integer.parseInt(layer.substring(i * 2, i * 2 + 1), 16);
                int low = Integer.parseInt(layer.substring(i * 2 + 1, i * 2 + 2), 16);
                byteContent[i] = (byte) (high * 16 + low);
            }

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, getKey(key));

            byte[] buf = cipher.doFinal(byteContent);
            return new String(buf, "utf-8");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Key getKey(String key) {
        try {
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(key.getBytes());
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(secureRandom);
            return generator.generateKey();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {

        String a = "a";
        String text = "编程ABCDefgh~！@#$%^&*()|'?>.<;1234567823401";
        String layer = encrypt(text, a);
        System.out.println("密文：" + layer);
        String plain = decrypt(layer, a);
        System.out.println("原文：" + plain);
    }

}

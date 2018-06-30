package com.denghb.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;

public class MD5Utils {

    public static String file(String filePath) {
        FileInputStream fis = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            fis = new FileInputStream(filePath);

            byte[] dataBytes = new byte[1024];

            int nread = 0;
            while ((nread = fis.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            }

            byte[] mdbytes = md.digest();
            return toHex(mdbytes);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (IOException e) {

            }
        }
        return null;
    }

    private static String toHex(byte[] bytes) {
        StringBuffer str = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xff & bytes[i]);
            if (hex.length() == 1) str.append('0');
            str.append(hex);
        }
        return str.toString();
    }

    private static String text(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(text.getBytes());
            byte mdbytes[] = md.digest();
            return toHex(mdbytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args)  {

        System.out.println(text("1234"));
        System.out.println(text("1234343"));
        System.out.println(text("123423"));

        System.out.println(file("/Users/denghb/Downloads/14.html"));
        System.out.println(file("/Users/denghb/tmp/14.html"));
    }
}

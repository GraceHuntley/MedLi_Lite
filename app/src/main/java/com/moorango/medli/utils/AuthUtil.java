package com.moorango.medli.utils;

import java.security.MessageDigest;

/**
 * Created by cmac147 on 7/14/15.
 */
public class AuthUtil {

    public static String encryptPassword(String password) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            messageDigest.update(password.getBytes("iso-8859-1"), 0, password.length());
            byte[] sha1hash = messageDigest.digest();
            return convertToHex(sha1hash);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String convertToHex(byte[] data) {
        StringBuilder result = new StringBuilder();
        for (byte aData : data) {
            result.append(Integer.toString((aData & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString();
    }
}

package com.ontimize.jee.core.common.security;

import java.io.UnsupportedEncodingException;

public class EncryptHelper {

    public static String encrypt(String s) throws IllegalArgumentException {
        if ((s == null) || (s.length() == 0)) {
            throw new IllegalArgumentException(
                    "Error: invalid string. If can not be null and the lenght must be greater than 0");
        }
        byte[] bytes = s.getBytes();
        byte[] res = new byte[bytes.length];
        byte[] llave = { 12, 67, 89, 124 };
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            byte bRes = b;
            for (int j = 0; j < llave.length; j++) {
                bRes = (byte) (bRes ^ llave[j]);
            }
            res[i] = bRes;
        }
        return new String(res);
    }

    public static byte[] encrypt(byte[] bytes, String key)
            throws IllegalArgumentException, UnsupportedEncodingException {
        if ((bytes == null) || (bytes.length == 0)) {
            throw new IllegalArgumentException(
                    "Error: invalid string. If can not be null and the lenght must be greater than 0");
        }
        byte[] res = new byte[bytes.length];
        byte[] llave = key.getBytes("ISO-8859-1");
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            byte bRes = b;
            for (int j = 0; j < llave.length; j++) {
                bRes = (byte) (bRes ^ llave[j]);
            }
            res[i] = bRes;
        }
        return res;
    }

    public static String encrypt(String password, String key, int number)
            throws IllegalArgumentException, UnsupportedEncodingException {
        if ((password == null) || (password.length() == 0)) {
            throw new IllegalArgumentException(
                    "Error: invalid string. If can not be null and the lenght must be greater than 0");
        }
        byte[] bytes = password.getBytes();
        byte[] res = new byte[bytes.length];
        byte[] llave = key.getBytes("ISO-8859-1");
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            byte bRes = b;
            for (int j = 0; j < llave.length; j++) {
                bRes = (byte) (bRes ^ llave[(j + number) % llave.length]);
            }
            res[i] = bRes;
        }
        return new String(res);
    }

}

package com.hnit.seckill.util;

import org.apache.commons.codec.digest.DigestUtils;

public class MD5Util {

    public static String md5(String src){
        return DigestUtils.md5Hex(src);
    }

    private static String salt = "1a2b3c4d";
    public static String inputPassFromPass(String pass){
        String str = "" + salt.charAt(0) + salt.charAt(1) + pass + salt.charAt(5) + salt.charAt(4);
        return md5(str);
    }
    public static String fromPass2DBPass(String pass,String salt){
        String str = "" + salt.charAt(0) + salt.charAt(1) + pass + salt.charAt(5) + salt.charAt(4);
        return md5(str);
    }
    public static String inputPass2DBPass(String pass,String salt){
        String  inputPass = inputPassFromPass(pass);
        String dbPass = fromPass2DBPass(inputPass,salt);
        return dbPass;
    }

    public static void main(String[] args) {
        System.out.println(inputPassFromPass("123456"));
        System.out.println(inputPass2DBPass("123456","1a2b3c4d"));
    }
}

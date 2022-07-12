package com.example.smartalbum.util;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * MD5 工具
 *
 * @author Boliang Weng
 */
public class MD5Utils {
    private static final Integer SALT = 10050099;
    public static String getMd5String(String value) {
        char[] chars = value.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char) (chars[i] + SALT);
        }
        String s = new String(chars);
        return DigestUtils.md5Hex(s);
    }
}

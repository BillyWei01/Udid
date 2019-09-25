package com.horizon.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Digest {
    public static String getShortMd5(byte[] msg) throws NoSuchAlgorithmException {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(md5(msg));
    }
    public static byte[] md5(byte[] msg) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("MD5").digest(msg);
    }

    public static byte[] sha1(byte[] msg) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("SHA-1").digest(msg);
    }

    public static byte[] sha256(byte[] msg) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("SHA-256").digest(msg);
    }

    public static byte[] sha512(byte[] msg) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("SHA-512").digest(msg);
    }

}

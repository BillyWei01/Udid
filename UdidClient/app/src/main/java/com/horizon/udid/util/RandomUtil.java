package com.horizon.udid.util;


import android.util.Base64;

import java.security.SecureRandom;

public class RandomUtil {

    private static class Holder {
        static final SecureRandom numberGenerator = new SecureRandom();
    }

    /**
     * 返回随机ID
     *
     * UUID.randomUUID() 返回字符串有36个字符，比较占用空间，
     * 而此函数返回的随机ID只有20个字节，且唯一性和 UUID.randomUUID() 是等效的
     */
    public static String randomUUID() {
        byte[] bytes = new byte[15];
        Holder.numberGenerator.nextBytes(bytes);
        return Base64.encodeToString(bytes, Base64.URL_SAFE | Base64.NO_WRAP);
    }
}

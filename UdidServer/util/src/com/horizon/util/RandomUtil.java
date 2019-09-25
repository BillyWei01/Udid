package com.horizon.util;



import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;

public class RandomUtil {
    public static final Random RANDOM = new Random();

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
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}

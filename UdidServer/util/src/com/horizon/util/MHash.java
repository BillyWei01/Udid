package com.horizon.util;


/**
 * murmurhash
 *
 * see:
 * https://sites.google.com/site/murmurhash/
 *
 */
public class MHash {
    public static long hash64(String str) {
        if (str == null || str.length() == 0) {
            return 0L;
        }
        byte[] bytes = str.getBytes();
        return hash64(bytes, bytes.length);
    }

    public static long hash64(final byte[] data, int len) {
        if (data == null || data.length == 0 || len == 0) {
            return 0L;
        }
        final long m = 0xc6a4a7935bd1e995L;
        final long seed = 0xe17a1465L;
        final int r = 47;

        long h = seed ^ (len * m);
        int remain = len & 7;
        int size = len - remain;

        for (int i = 0; i < size; i += 8) {
            long k = ((long) data[i+7] << 56) +
                    ((long) (data[i + 6] & 0xFF) << 48) +
                    ((long) (data[i + 5] & 0xFF) << 40) +
                    ((long) (data[i + 4] & 0xFF) << 32) +
                    ((long) (data[i + 3] & 0xFF) << 24) +
                    ((data[i + 2] & 0xFF) << 16) +
                    ((data[i + 1] & 0xFF) << 8) +
                    ((data[i] & 0xFF));
            k *= m;
            k ^= k >>> r;
            k *= m;
            h ^= k;
            h *= m;
        }

        switch (remain) {
            case 7:
                h ^= (long) (data[size + 6] & 0xFF) << 48;
            case 6:
                h ^= (long) (data[size + 5] & 0xFF) << 40;
            case 5:
                h ^= (long) (data[size + 4] & 0xFF) << 32;
            case 4:
                h ^= (long) (data[size + 3] & 0xFF) << 24;
            case 3:
                h ^= (data[size + 2] & 0xFF) << 16;
            case 2:
                h ^= (data[size + 1] & 0xFF) << 8;
            case 1:
                h ^= (data[size] & 0xFF);
                h *= m;
        }

        h ^= h >>> r;
        h *= m;
        h ^= h >>> r;

        return h;
    }
}

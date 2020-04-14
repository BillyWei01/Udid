package horizon.util;

/**
 * 基本思路和 AES 相似，包含混淆层，扩散层，和轮加密。
 * 轮加密所用密钥不需要经过密钥扩展，而是直接用 SecureRandom 生成。
 * 若使用，请记得生成自己的密钥。
 *
 * 参考文档：
 * https://juejin.im/post/5e5b43ca51882549361e5d32
 * https://github.com/No89757/LongEncrypt
 */
public class LongEncoder {
    /*
    // Generate Keys
    private static void getKey(){
        SecureRandom r = new SecureRandom();
        int round = 8;
        byte[] bytes = new byte[(round + 1) * 8];
        r.nextBytes(bytes);
        for (int i = 0; i < round + 1; i++) {
            for (int j = 0; j < 8; j++) {
                System.out.print(bytes[i*8+j] +", ");
            }
            System.out.println();
        }
    }
    */
    private static final int ROUND = 8;

    private static final byte[] S_BOX = {
            99, 124, 119, 123, -14, 107, 111, -59, 48, 1, 103, 43, -2, -41, -85, 118,
            -54, -126, -55, 125, -6, 89, 71, -16, -83, -44, -94, -81, -100, -92, 114, -64,
            -73, -3, -109, 38, 54, 63, -9, -52, 52, -91, -27, -15, 113, -40, 49, 21,
            4, -57, 35, -61, 24, -106, 5, -102, 7, 18, -128, -30, -21, 39, -78, 117,
            9, -125, 44, 26, 27, 110, 90, -96, 82, 59, -42, -77, 41, -29, 47, -124,
            83, -47, 0, -19, 32, -4, -79, 91, 106, -53, -66, 57, 74, 76, 88, -49,
            -48, -17, -86, -5, 67, 77, 51, -123, 69, -7, 2, 127, 80, 60, -97, -88,
            81, -93, 64, -113, -110, -99, 56, -11, -68, -74, -38, 33, 16, -1, -13, -46,
            -51, 12, 19, -20, 95, -105, 68, 23, -60, -89, 126, 61, 100, 93, 25, 115,
            96, -127, 79, -36, 34, 42, -112, -120, 70, -18, -72, 20, -34, 94, 11, -37,
            -32, 50, 58, 10, 73, 6, 36, 92, -62, -45, -84, 98, -111, -107, -28, 121,
            -25, -56, 55, 109, -115, -43, 78, -87, 108, 86, -12, -22, 101, 122, -82, 8,
            -70, 120, 37, 46, 28, -90, -76, -58, -24, -35, 116, 31, 75, -67, -117, -118,
            112, 62, -75, 102, 72, 3, -10, 14, 97, 53, 87, -71, -122, -63, 29, -98,
            -31, -8, -104, 17, 105, -39, -114, -108, -101, 30, -121, -23, -50, 85, 40, -33,
            -116, -95, -119, 13, -65, -26, 66, 104, 65, -103, 45, 15, -80, 84, -69, 22
    };

    private static final byte[] INV_S_BOX = {
            82, 9, 106, -43, 48, 54, -91, 56, -65, 64, -93, -98, -127, -13, -41, -5,
            124, -29, 57, -126, -101, 47, -1, -121, 52, -114, 67, 68, -60, -34, -23, -53,
            84, 123, -108, 50, -90, -62, 35, 61, -18, 76, -107, 11, 66, -6, -61, 78,
            8, 46, -95, 102, 40, -39, 36, -78, 118, 91, -94, 73, 109, -117, -47, 37,
            114, -8, -10, 100, -122, 104, -104, 22, -44, -92, 92, -52, 93, 101, -74, -110,
            108, 112, 72, 80, -3, -19, -71, -38, 94, 21, 70, 87, -89, -115, -99, -124,
            -112, -40, -85, 0, -116, -68, -45, 10, -9, -28, 88, 5, -72, -77, 69, 6,
            -48, 44, 30, -113, -54, 63, 15, 2, -63, -81, -67, 3, 1, 19, -118, 107,
            58, -111, 17, 65, 79, 103, -36, -22, -105, -14, -49, -50, -16, -76, -26, 115,
            -106, -84, 116, 34, -25, -83, 53, -123, -30, -7, 55, -24, 28, 117, -33, 110,
            71, -15, 26, 113, 29, 41, -59, -119, 111, -73, 98, 14, -86, 24, -66, 27,
            -4, 86, 62, 75, -58, -46, 121, 32, -102, -37, -64, -2, 120, -51, 90, -12,
            31, -35, -88, 51, -120, 7, -57, 49, -79, 18, 16, 89, 39, -128, -20, 95,
            96, 81, 127, -87, 25, -75, 74, 13, 45, -27, 122, -97, -109, -55, -100, -17,
            -96, -32, 59, 77, -82, 42, -11, -80, -56, -21, -69, 60, -125, 83, -103, 97,
            23, 43, 4, 126, -70, 119, -42, 38, -31, 105, 20, 99, 85, 33, 12, 125
    };

    private static final byte[] KEY = {
            -14, 40, 52, -119, -126, -47, 74, 73,
            -124, 81, -14, 116, 107, -5, 89, -97,
            49, 93, -121, -40, -55, -107, 117, 83,
            65, 92, -2, -51, 8, 111, 106, 84,
            44, 53, -29, -52, -47, -33, -2, -45,
            119, 58, 50, -39, 41, -41, -124, -19,
            -27, 102, -84, 58, -43, -86, -98, 9,
            -93, -102, 58, 117, 7, -69, 24, -49,
            -17, -111, 81, -56, 6, 55, 121, -23};

    private static byte mul2(byte a) {
        return (byte) (((a & 0x80) != 0) ? ((a << 1) ^ 0x1b) : (a << 1));
    }

    public static void mix_column(byte[] s, int i) {
        /*
         * 左乘置换矩阵
         * [b0]	  [02 03 01 01]   [s0]
         * [b1]	= [01 02 03 01] . [s1]
         * [b2]	  [01 01 02 03]   [s2]
         * [b3]	  [03 01 01 02]   [s3]
         */
        byte t = (byte) (s[i] ^ s[1 + i] ^ s[2 + i] ^ s[3 + i]);
        byte b0 = (byte) (mul2((byte) (s[i] ^ s[1 + i])) ^ s[i] ^ t);
        byte b1 = (byte) (mul2((byte) (s[1 + i] ^ s[2 + i])) ^ s[1 + i] ^ t);
        byte b2 = (byte) (mul2((byte) (s[2 + i] ^ s[3 + i])) ^ s[2 + i] ^ t);
        byte b3 = (byte) (mul2((byte) (s[3 + i] ^ s[i])) ^ s[3 + i] ^ t);

        s[i] = b0;
        s[1 + i] = b1;
        s[2 + i] = b2;
        s[3 + i] = b3;
    }

    public static void inv_mix_column(byte[] s, int i) {
        /*
         * 左乘置换矩阵的逆矩阵
         * [d0]	  [0e 0b 0d 09]   [b0]
         * [d1]	= [09 0e 0b 0d] . [b1]
         * [d2]	  [0d 09 0e 0b]   [b2]
         * [d3]	  [0b 0d 09 0e]   [b3]
         */
        byte t, u, v;
        t = (byte) (s[i] ^ s[1 + i] ^ s[2 + i] ^ s[3 + i]);
        byte b0 = (byte) (t ^ s[i] ^ mul2((byte) (s[i] ^ s[1 + i])));
        byte b1 = (byte) (t ^ s[1 + i] ^ mul2((byte) (s[1 + i] ^ s[2 + i])));
        byte b2 = (byte) (t ^ s[2 + i] ^ mul2((byte) (s[2 + i] ^ s[3 + i])));
        byte b3 = (byte) (t ^ s[3 + i] ^ mul2((byte) (s[3 + i] ^ s[i])));
        u = mul2(mul2((byte) (s[i] ^ s[2 + i])));
        v = mul2(mul2((byte) (s[1 + i] ^ s[3 + i])));
        t = mul2((byte) (u ^ v));
        b0 ^= t ^ u;
        b1 ^= t ^ v;
        b2 ^= t ^ u;
        b3 ^= t ^ v;

        s[i] = b0;
        s[1 + i] = b1;
        s[2 + i] = b2;
        s[3 + i] = b3;
    }

    private static void shift_rows(byte[] state) {
        byte t1 = state[7];
        byte t0 = state[6];
        state[7] = state[5];
        state[6] = state[4];
        state[5] = state[3];
        state[4] = state[2];
        state[3] = state[1];
        state[2] = state[0];
        state[1] = t1;
        state[0] = t0;
    }

    private static void inv_shift_rows(byte[] state) {
        byte t0 = state[0];
        byte t1 = state[1];
        state[0] = state[2];
        state[1] = state[3];
        state[2] = state[4];
        state[3] = state[5];
        state[4] = state[6];
        state[5] = state[7];
        state[6] = t0;
        state[7] = t1;
    }

    public static long encode64(long value) {
        byte[] state = long2Bytes(value);
        for (int i = 0; i < ROUND; i++) {
            for (int j = 0; j < 8; j++) {
                int m = ((i << 3) + j);
                // AddRoundKey and SubBytes
                state[j] = S_BOX[(state[j] ^ KEY[m]) & 0xFF];
            }
            shift_rows(state);
            mix_column(state, 0);
            mix_column(state, 4);
        }
        for (int j = 0; j < 8; j++) {
            state[j] ^= KEY[(ROUND << 3) + j];
        }
        return bytes2Long(state);
    }

    public static long decode64(long value) {
        byte[] state = long2Bytes(value);
        for (int j = 0; j < 8; j++) {
            state[j] ^= KEY[(ROUND << 3) + j];
        }
        for (int i = ROUND - 1; i >= 0; i--) {
            inv_mix_column(state, 0);
            inv_mix_column(state, 4);
            inv_shift_rows(state);
            for (int j = 0; j < 8; j++) {
                int m = ((i << 3) + j);
                state[j] = (byte) (INV_S_BOX[state[j] & 0xFF] ^ KEY[m]);
            }
        }
        return bytes2Long(state);
    }

    public static long encode48(long value) {
        byte[] state = long2Bytes(value);
        for (int i = 0; i < ROUND; i++) {
            for (int j = 0; j < 6; j++) {
                int m = ((i << 3) + j);
                // AddRoundKey and SubBytes
                state[j] = S_BOX[(state[j] ^ KEY[m]) & 0xFF];
            }
            // 对于48bit的输入而言，就不需要ShiftRows了
            // 因为先后对[0,3], [2,5]进行MixColumns已经可以对整个输入扩散了
            mix_column(state, 0);
            mix_column(state, 2);
        }
        for (int j = 0; j < 6; j++) {
            state[j] ^= KEY[(ROUND << 3) + j];
        }
        // 输出的Long，高位的两个字节没有变
        // 所以如果输入时小于2^48的数值，则输出也是小于2^48的数组
        return bytes2Long(state);
    }

    public static long decode48(long value) {
        byte[] state = long2Bytes(value);
        byte[] buf = new byte[6];
        for (int j = 0; j < 6; j++) {
            state[j] ^= KEY[(ROUND << 3) + j];
        }
        for (int i = ROUND - 1; i >= 0; i--) {
            inv_mix_column(state, 2);
            inv_mix_column(state, 0);
            for (int j = 0; j < 6; j++) {
                int m = ((i << 3) + j);
                state[j] = (byte) (INV_S_BOX[state[j] & 0xFF] ^ KEY[m]);
            }
        }
        return bytes2Long(state);
    }

    public static byte[] long2Bytes(long value) {
        byte[] state = new byte[8];
        state[7] = (byte) ((value >> 56) & 0xFF);
        state[6] = (byte) ((value >> 48) & 0xFF);
        state[5] = (byte) ((value >> 40) & 0xFF);
        state[4] = (byte) ((value >> 32) & 0xFF);
        state[3] = (byte) ((value >> 24) & 0xFF);
        state[2] = (byte) ((value >> 16) & 0xFF);
        state[1] = (byte) ((value >> 8) & 0xFF);
        state[0] = (byte) (value & 0xFF);
        return state;
    }

    public static long bytes2Long(byte[] state) {
        return (((long) state[7]) << 56) +
                ((long) (state[6] & 0xFF) << 48) +
                ((long) (state[5] & 0xFF) << 40) +
                ((long) (state[4] & 0xFF) << 32) +
                ((long) (state[3] & 0xFF) << 24) +
                ((long) (state[2] & 0xFF) << 16) +
                ((long) (state[1] & 0xFF) << 8) +
                ((long) (state[0] & 0xFF));
    }
}

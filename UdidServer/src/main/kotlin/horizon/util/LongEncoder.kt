package horizon.util

/**
 * 基本思路和 AES 相似，包含混淆层，扩散层，和轮加密。
 * 轮加密所用密钥不需要经过密钥扩展，而是直接用 SecureRandom 生成。
 * 若使用，请记得生成自己的密钥。
 */
object LongEncoder {
    /*
    // generate Keys,
    private static void getKey(){
        SecureRandom r = new SecureRandom();
        int round = 4;
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
    private const val ROUND = 4
    private val S_BOX = byteArrayOf(
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
    )
    private val INV_S_BOX = byteArrayOf(
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
    )
    private val mul2 = byteArrayOf(
        0, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30,
        32, 34, 36, 38, 40, 42, 44, 46, 48, 50, 52, 54, 56, 58, 60, 62,
        64, 66, 68, 70, 72, 74, 76, 78, 80, 82, 84, 86, 88, 90, 92, 94,
        96, 98, 100, 102, 104, 106, 108, 110, 112, 114, 116, 118, 120, 122, 124, 126,
        -128, -126, -124, -122, -120, -118, -116, -114, -112, -110, -108, -106, -104, -102, -100, -98,
        -96, -94, -92, -90, -88, -86, -84, -82, -80, -78, -76, -74, -72, -70, -68, -66,
        -64, -62, -60, -58, -56, -54, -52, -50, -48, -46, -44, -42, -40, -38, -36, -34,
        -32, -30, -28, -26, -24, -22, -20, -18, -16, -14, -12, -10, -8, -6, -4, -2,
        27, 25, 31, 29, 19, 17, 23, 21, 11, 9, 15, 13, 3, 1, 7, 5,
        59, 57, 63, 61, 51, 49, 55, 53, 43, 41, 47, 45, 35, 33, 39, 37,
        91, 89, 95, 93, 83, 81, 87, 85, 75, 73, 79, 77, 67, 65, 71, 69,
        123, 121, 127, 125, 115, 113, 119, 117, 107, 105, 111, 109, 99, 97, 103, 101,
        -101, -103, -97, -99, -109, -111, -105, -107, -117, -119, -113, -115, -125, -127, -121, -123,
        -69, -71, -65, -67, -77, -79, -73, -75, -85, -87, -81, -83, -93, -95, -89, -91,
        -37, -39, -33, -35, -45, -47, -41, -43, -53, -55, -49, -51, -61, -63, -57, -59,
        -5, -7, -1, -3, -13, -15, -9, -11, -21, -23, -17, -19, -29, -31, -25, -27
    )
    private val KEY = byteArrayOf(
        -14, 40, 52, -119, -126, -47, 74, 73,
        -124, 81, -14, 116, 107, -5, 89, -97,
        49, 93, -121, -40, -55, -107, 117, 83,
        65, 92, -2, -51, 8, 111, 106, 84,
        44, 53, -29, -52, -47, -33, -2, -45
    )

    /*
     * [b0]	  [02 03 01 01]   [b0]
     * [b1]	= [01 02 03 01] . [b1]
     * [b2]	  [01 01 02 03]   [b2]
     * [b3]	  [03 01 01 02]   [b3]
     */
    private fun multiply(b: ByteArray) {
        val a0 = (b[0].toInt() xor b[1].toInt()).toByte()
        val a1 = (b[1].toInt() xor b[2].toInt()).toByte()
        val a2 = (b[2].toInt() xor b[3].toInt()).toByte()
        val a3 = (b[3].toInt() xor b[0].toInt()).toByte()
        val t = (a0.toInt() xor a2.toInt()).toByte()
        b[0] = (b[0].toInt() xor (mul2[a0.toInt() and 0xFF].toInt() xor t.toInt())).toByte()
        b[1] = (b[1].toInt() xor (mul2[a1.toInt() and 0xFF].toInt() xor t.toInt())).toByte()
        b[2] = (b[2].toInt() xor (mul2[a2.toInt() and 0xFF].toInt() xor t.toInt())).toByte()
        b[3] = (b[3].toInt() xor (mul2[a3.toInt() and 0xFF].toInt() xor t.toInt())).toByte()
    }

    private fun multiply_2(b: ByteArray) {
        val a0 = (b[2].toInt() xor b[3].toInt()).toByte()
        val a1 = (b[3].toInt() xor b[4].toInt()).toByte()
        val a2 = (b[4].toInt() xor b[5].toInt()).toByte()
        val a3 = (b[5].toInt() xor b[2].toInt()).toByte()
        val t = (a0.toInt() xor a2.toInt()).toByte()
        b[2] = (b[2].toInt() xor (mul2[a0.toInt() and 0xFF].toInt() xor t.toInt())).toByte()
        b[3] = (b[3].toInt() xor (mul2[a1.toInt() and 0xFF].toInt() xor t.toInt())).toByte()
        b[4] = (b[4].toInt() xor (mul2[a2.toInt() and 0xFF].toInt() xor t.toInt())).toByte()
        b[5] = (b[5].toInt() xor (mul2[a3.toInt() and 0xFF].toInt() xor t.toInt())).toByte()
    }

    private fun multiply_4(b: ByteArray) {
        val a0 = (b[4].toInt() xor b[5].toInt()).toByte()
        val a1 = (b[5].toInt() xor b[6].toInt()).toByte()
        val a2 = (b[6].toInt() xor b[7].toInt()).toByte()
        val a3 = (b[7].toInt() xor b[4].toInt()).toByte()
        val t = (a0.toInt() xor a2.toInt()).toByte()
        b[4] = (b[4].toInt() xor (mul2[a0.toInt() and 0xFF].toInt() xor t.toInt())).toByte()
        b[5] = (b[5].toInt() xor (mul2[a1.toInt() and 0xFF].toInt() xor t.toInt())).toByte()
        b[6] = (b[6].toInt() xor (mul2[a2.toInt() and 0xFF].toInt() xor t.toInt())).toByte()
        b[7] = (b[7].toInt() xor (mul2[a3.toInt() and 0xFF].toInt() xor t.toInt())).toByte()
    }

    /*
     * [d0]	  [0e 0b 0d 09]   [b0]
     * [d1]	= [09 0e 0b 0d] . [b1]
     * [d2]	  [0d 09 0e 0b]   [b2]
     * [d3]	  [0b 0d 09 0e]   [b3]
     */
    private fun inv_multiply(b: ByteArray, i: Int) {
        var u = (b[i].toInt() xor b[i + 2].toInt()).toByte()
        var v = (b[i + 1].toInt() xor b[i + 3].toInt()).toByte()
        if (i == 0) {
            multiply(b)
        } else if (i == 2) {
            multiply_2(b)
        } else if (i == 4) {
            multiply_4(b)
        } else {
            throw IllegalArgumentException("invalid i:$i")
        }
        u = mul2[mul2[u.toInt() and 0xFF].toInt() and 0xFF]
        v = mul2[mul2[v.toInt() and 0xFF].toInt() and 0xFF]
        val t = mul2[u.toInt() xor v.toInt() and 0xFF]
        u = (u.toInt() xor t.toInt()).toByte()
        v = (v.toInt() xor t.toInt()).toByte()
        b[i] = (b[i].toInt() xor u.toInt()).toByte()
        b[i + 1] = (b[i + 1].toInt() xor v.toInt()).toByte()
        b[i + 2] = (b[i + 2].toInt() xor u.toInt()).toByte()
        b[i + 3] = (b[i + 3].toInt() xor v.toInt()).toByte()
    }

    private fun shift_rows(state: ByteArray) {
        val t1 = state[7]
        val t0 = state[6]
        state[7] = state[5]
        state[6] = state[4]
        state[5] = state[3]
        state[4] = state[2]
        state[3] = state[1]
        state[2] = state[0]
        state[1] = t1
        state[0] = t0
    }

    private fun inv_shift_rows(state: ByteArray) {
        val t0 = state[0]
        val t1 = state[1]
        state[0] = state[2]
        state[1] = state[3]
        state[2] = state[4]
        state[3] = state[5]
        state[4] = state[6]
        state[5] = state[7]
        state[6] = t0
        state[7] = t1
    }

    /*  public static long encode64(long value) {
        byte[] state = long2Bytes(value);
        for (int i = 0; i < ROUND; i++) {
            for (int j = 0; j < 8; j++) {
                int m = ((i << 3) + j);
                // AddRoundKey and SubBytes
                state[j] = S_BOX[(state[j] ^ KEY[m]) & 0xFF];
            }
            shift_rows(state);
            multiply(state);
            multiply_4(state);
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
            inv_multiply(state, 0);
            inv_multiply(state, 4);
            inv_shift_rows(state);
            for (int j = 0; j < 8; j++) {
                int m = ((i << 3) + j);
                state[j] = (byte) (INV_S_BOX[state[j] & 0xFF] ^ KEY[m]);
            }
        }
        return bytes2Long(state);
    }
*/
    fun encode48(value: Long): Long {
        val state = long2Bytes(value)
        for (i in 0 until ROUND) {
            for (j in 0..5) {
                val m = (i shl 3) + j
                // AddRoundKey and SubBytes
                state[j] = S_BOX[state[j].toInt() xor KEY[m].toInt() and 0xFF]
            }
            // 对于48bit的输入而言，就不需要ShiftRows了
            // 因为先后对[0,3], [2,5]进行MixColumns已经可以对整个输入扩散了
            multiply(state)
            multiply_2(state)
        }
        for (j in 0..5) {
            state[j] = (state[j].toInt() xor KEY[(ROUND shl 3) + j].toInt()).toByte()
        }
        // 输出的Long，高位的两个字节没有变
        // 所以如果输入时小于2^48的数值，则输出也是小于2^48的数组
        return bytes2Long(state)
    }

    /*    public static long decode48(long value) {
        byte[] state = long2Bytes(value);
        for (int j = 0; j < 6; j++) {
            state[j] ^= KEY[(ROUND << 3) + j];
        }
        for (int i = ROUND - 1; i >= 0; i--) {
            inv_multiply(state, 2);
            inv_multiply(state, 0);
            for (int j = 0; j < 6; j++) {
                int m = ((i << 3) + j);
                state[j] = (byte) (INV_S_BOX[state[j] & 0xFF] ^ KEY[m]);
            }
        }
        return bytes2Long(state);
    }*/
    fun long2Bytes(value: Long): ByteArray {
        val state = ByteArray(8)
        state[7] = (value shr 56 and 0xFFL).toByte()
        state[6] = (value shr 48 and 0xFFL).toByte()
        state[5] = (value shr 40 and 0xFFL).toByte()
        state[4] = (value shr 32 and 0xFFL).toByte()
        state[3] = (value shr 24 and 0xFFL).toByte()
        state[2] = (value shr 16 and 0xFFL).toByte()
        state[1] = (value shr 8 and 0xFFL).toByte()
        state[0] = (value and 0xFFL).toByte()
        return state
    }

    fun bytes2Long(state: ByteArray): Long {
        return (state[7].toLong() shl 56) +
                ((state[6].toInt() and 0xFF).toLong() shl 48) +
                ((state[5].toInt() and 0xFF).toLong() shl 40) +
                ((state[4].toInt() and 0xFF).toLong() shl 32) +
                ((state[3].toInt() and 0xFF).toLong() shl 24) +
                ((state[2].toInt() and 0xFF).toLong() shl 16) +
                ((state[1].toInt() and 0xFF).toLong() shl 8) +
                (state[0].toInt() and 0xFF).toLong()
    }
}

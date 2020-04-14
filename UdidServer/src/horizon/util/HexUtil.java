package horizon.util;

public class HexUtil {
    private static final char[] HEX_DIGITS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f'};

    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    /**
     * 小于2^48的long数值转十六进制字符串
     * @param n long类型整数
     * @return 12字节的字符串（十六进制）
     */
    public static String long48ToHex(long n) {
        if((n >>> 48) > 0){
            throw new IllegalArgumentException(n + " is bigger than 2^48");
        }
        char[] buf = new char[12];
        for (int i = 5; i >= 0; i--) {
            int b = (int) n;
            int index = i << 1;
            buf[index] = HEX_DIGITS[(b >> 4) & 0xF];
            buf[index + 1] = HEX_DIGITS[b & 0xF];
            n = n >>> 8;
        }
        return new String(buf);
    }

    /**
     * 十六进制字符串转long类型整数
     * @param hex 12字节的字符串（十六进制）
     * @return long类型整数
     */
    public static long hexToLong48(String hex) {
        if (hex == null || hex.isEmpty()) {
            return 0L;
        }
        byte[] buf = hex.getBytes();
        int len = buf.length;
        if (len != 12) {
            throw new NumberFormatException("invalid hex number, must be length of 12");
        }

        long a = 0L;
        for (byte b : buf) {
            a <<= 4;
            a |= byte2Int(b);
        }
        return a;
    }

    public static String long2Hex(long a) {
        char[] buf = new char[16];
        for (int i = 7; i >= 0; i--) {
            int b = (int) a;
            int index = i << 1;
            buf[index] = HEX_DIGITS[(b >> 4) & 0xF];
            buf[index + 1] = HEX_DIGITS[b & 0xF];
            a = a >>> 8;
        }
        int offset = 15;
        for (int i = 0; i < 16; i++) {
            if (buf[i] != '0') {
                offset = i;
                break;
            }
        }
        return new String(buf, offset, 16 - offset);
    }

    public static long hex2Long(String hex) {
        if (hex == null || hex.isEmpty()) {
            return 0L;
        }
        byte[] buf = hex.getBytes();
        int len = buf.length;
        if (len > 16) {
            throw new NumberFormatException("invalid hex number");
        }

        long a = 0L;
        for (byte b : buf) {
            a <<= 4;
            a |= byte2Int(b);
        }
        return a;
    }

    public static String bytes2Hex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        int len = bytes.length;
        char[] buf = new char[len << 1];
        for (int i = 0; i < len; i++) {
            int b = bytes[i];
            int index = i << 1;
            buf[index] = HEX_DIGITS[(b >> 4) & 0xF];
            buf[index + 1] = HEX_DIGITS[b & 0xF];
        }
        return new String(buf);
    }

    public static byte[] hex2Bytes(String hex) {
        if (hex == null || hex.isEmpty()) {
            return EMPTY_BYTE_ARRAY;
        }
        byte[] bytes = hex.getBytes();
        if ((bytes.length & 1) != 0) {
            throw new IllegalArgumentException("only support even length hex string");
        }
        int n = bytes.length >> 1;
        byte[] buf = new byte[n];
        for (int i = 0; i < n; i++) {
            int index = i << 1;
            buf[i] = (byte) ((byte2Int(bytes[index]) << 4) | byte2Int(bytes[index + 1]));
        }
        return buf;
    }

    private static int byte2Int(byte b) {
        if (b >= '0' && b <= '9') {
            return b - '0';
        } else if (b >= 'a' && b <= 'f') {
            return b - 'a' + 10;
        } else if (b >= 'A' && b <= 'F') {
            return b - 'A' + 10;
        } else {
            throw new NumberFormatException("invalid hex number");
        }
    }

}

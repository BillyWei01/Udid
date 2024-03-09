package horizon.util

object HexUtil {
    private val HEX_DIGITS = charArrayOf(
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'a', 'b', 'c', 'd', 'e', 'f'
    )
    private val EMPTY_BYTE_ARRAY = ByteArray(0)

    /**
     * 小于2^48的long数值转十六进制字符串
     * @param n long类型整数
     * @return 12字节的字符串（十六进制）
     */
    fun long48ToHex(n: Long): String {
        var n = n
        require(!(n ushr 48 > 0)) { "$n is bigger than 2^48" }
        val buf = CharArray(12)
        for (i in 5 downTo 0) {
            val b = n.toInt()
            val index = i shl 1
            buf[index] = HEX_DIGITS[(b shr 4) and 0xF]
            buf[index + 1] = HEX_DIGITS[b and 0xF]
            n = n ushr 8
        }
        return String(buf)
    }

    /**
     * 十六进制字符串转long类型整数
     * @param hex 12字节的字符串（十六进制）
     * @return long类型整数
     */
    fun hexToLong48(hex: String?): Long {
        if (hex == null || hex.isEmpty()) {
            return 0L
        }
        val buf = hex.toByteArray()
        val len = buf.size
        if (len != 12) {
            throw NumberFormatException("invalid hex number, must be length of 12")
        }
        var a = 0L
        for (b in buf) {
            a = a shl 4
            a = a or byte2Int(b).toLong()
        }
        return a
    }

    fun long2Hex(a: Long): String {
        var a = a
        val buf = CharArray(16)
        for (i in 7 downTo 0) {
            val b = a.toInt()
            val index = i shl 1
            buf[index] = HEX_DIGITS[(b shr 4) and 0xF]
            buf[index + 1] = HEX_DIGITS[b and 0xF]
            a = a ushr 8
        }
        var offset = 15
        for (i in 0..15) {
            if (buf[i] != '0') {
                offset = i
                break
            }
        }
        return String(buf, offset, 16 - offset)
    }

    fun hex2Long(hex: String?): Long {
        if (hex == null || hex.isEmpty()) {
            return 0L
        }
        val buf = hex.toByteArray()
        val len = buf.size
        if (len > 16) {
            throw NumberFormatException("invalid hex number")
        }
        var a = 0L
        for (b in buf) {
            a = a shl 4
            a = a or byte2Int(b).toLong()
        }
        return a
    }

    fun bytes2Hex(bytes: ByteArray?): String {
        if (bytes == null || bytes.size == 0) {
            return ""
        }
        val len = bytes.size
        val buf = CharArray(len shl 1)
        for (i in 0 until len) {
            val b = bytes[i].toInt()
            val index = i shl 1
            buf[index] = HEX_DIGITS[(b shr 4) and 0xF]
            buf[index + 1] = HEX_DIGITS[b and 0xF]
        }
        return String(buf)
    }

    fun hex2Bytes(hex: String?): ByteArray {
        if (hex == null || hex.isEmpty()) {
            return EMPTY_BYTE_ARRAY
        }
        val bytes = hex.toByteArray()
        if (bytes.size and 1 != 0) {
            throw IllegalArgumentException("only support even length hex string")
        }
        val n = bytes.size shr 1
        val buf = ByteArray(n)
        for (i in 0 until n) {
            val index = i shl 1
            buf[i] = (byte2Int(bytes[index]) shl 4 or byte2Int(bytes[index + 1])).toByte()
        }
        return buf
    }

    private fun byte2Int(b: Byte): Int {
        return if (b >= '0'.code.toByte() && b <= '9'.code.toByte()) {
            b - '0'.code.toByte()
        } else if (b >= 'a'.code.toByte() && b <= 'f'.code.toByte()) {
            b - 'a'.code.toByte() + 10
        } else if (b >= 'A'.code.toByte() && b <= 'F'.code.toByte()) {
            b - 'A'.code.toByte() + 10
        } else {
            throw NumberFormatException("invalid hex number")
        }
    }
}

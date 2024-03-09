package horizon.util

/**
 * murmurhash
 *
 * see:
 * https://sites.google.com/site/murmurhash/
 *
 */
object MHash {
    fun hash64(str: String?): Long {
        if (str == null || str.length == 0) {
            return 0L
        }
        val bytes = str.toByteArray()
        return hash64(bytes, bytes.size)
    }

    fun hash64(data: ByteArray?, len: Int): Long {
        if (data == null || data.size == 0 || len == 0) {
            return 0L
        }
        val m = -0x395b586ca42e166bL
        val seed = 0xe17a1465L
        val r = 47
        var h = seed xor len * m
        val remain = len and 7
        val size = len - remain
        var i = 0
        while (i < size) {
            var k = (data[i + 7].toLong() shl 56) +
                    ((data[i + 6].toInt() and 0xFF).toLong() shl 48) +
                    ((data[i + 5].toInt() and 0xFF).toLong() shl 40) +
                    ((data[i + 4].toInt() and 0xFF).toLong() shl 32) +
                    ((data[i + 3].toInt() and 0xFF).toLong() shl 24) +
                    (data[i + 2].toInt() and 0xFF shl 16) +
                    (data[i + 1].toInt() and 0xFF shl 8) +
                    (data[i].toInt() and 0xFF)
            k *= m
            k = k xor (k ushr r)
            k *= m
            h = h xor k
            h *= m
            i += 8
        }
        when (remain) {
            7 -> {
                h = h xor ((data[size + 6].toInt() and 0xFF).toLong() shl 48)
                h = h xor ((data[size + 5].toInt() and 0xFF).toLong() shl 40)
                h = h xor ((data[size + 4].toInt() and 0xFF).toLong() shl 32)
                h = h xor ((data[size + 3].toInt() and 0xFF).toLong() shl 24)
                h = h xor (data[size + 2].toInt() and 0xFF shl 16).toLong()
                h = h xor (data[size + 1].toInt() and 0xFF shl 8).toLong()
                h = h xor (data[size].toInt() and 0xFF).toLong()
                h *= m
            }

            6 -> {
                h = h xor ((data[size + 5].toInt() and 0xFF).toLong() shl 40)
                h = h xor ((data[size + 4].toInt() and 0xFF).toLong() shl 32)
                h = h xor ((data[size + 3].toInt() and 0xFF).toLong() shl 24)
                h = h xor (data[size + 2].toInt() and 0xFF shl 16).toLong()
                h = h xor (data[size + 1].toInt() and 0xFF shl 8).toLong()
                h = h xor (data[size].toInt() and 0xFF).toLong()
                h *= m
            }

            5 -> {
                h = h xor ((data[size + 4].toInt() and 0xFF).toLong() shl 32)
                h = h xor ((data[size + 3].toInt() and 0xFF).toLong() shl 24)
                h = h xor (data[size + 2].toInt() and 0xFF shl 16).toLong()
                h = h xor (data[size + 1].toInt() and 0xFF shl 8).toLong()
                h = h xor (data[size].toInt() and 0xFF).toLong()
                h *= m
            }

            4 -> {
                h = h xor ((data[size + 3].toInt() and 0xFF).toLong() shl 24)
                h = h xor (data[size + 2].toInt() and 0xFF shl 16).toLong()
                h = h xor (data[size + 1].toInt() and 0xFF shl 8).toLong()
                h = h xor (data[size].toInt() and 0xFF).toLong()
                h *= m
            }

            3 -> {
                h = h xor (data[size + 2].toInt() and 0xFF shl 16).toLong()
                h = h xor (data[size + 1].toInt() and 0xFF shl 8).toLong()
                h = h xor (data[size].toInt() and 0xFF).toLong()
                h *= m
            }

            2 -> {
                h = h xor (data[size + 1].toInt() and 0xFF shl 8).toLong()
                h = h xor (data[size].toInt() and 0xFF).toLong()
                h *= m
            }

            1 -> {
                h = h xor (data[size].toInt() and 0xFF).toLong()
                h *= m
            }
        }
        h = h xor (h ushr r)
        h *= m
        h = h xor (h ushr r)
        return h
    }
}

package horizon.util

object StringUtil {
    fun removeChar(src: String?, ch: Char): String? {
        if (src == null || src.isEmpty()) {
            return src
        }
        if (src.indexOf(ch) < 0) {
            return src
        }
        val a = src.toCharArray()
        val len = a.size
        var p = 0
        for (i in 0 until len) {
            if (a[i] != ch) {
                a[p++] = a[i]
            }
        }
        return String(a, 0, p)
    }
}

package horizon.util

import java.text.SimpleDateFormat

object DateUtil {
    private val sDateFormatter: ThreadLocal<SimpleDateFormat> = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue(): SimpleDateFormat {
            return SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS")
        }
    }

    fun now(): String {
        return sDateFormatter.get().format(System.currentTimeMillis())
    }
}

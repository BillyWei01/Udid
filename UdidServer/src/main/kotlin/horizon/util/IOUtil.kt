package horizon.util

import java.io.*
import java.nio.charset.StandardCharsets

object IOUtil {
    @JvmStatic
    @Throws(IOException::class)
    fun makeFileIfNotExist(file: File): Boolean {
        return if (file.isFile) {
            true
        } else {
            val parent = file.parentFile
            parent != null && (parent.isDirectory || parent.mkdirs()) && file.createNewFile()
        }
    }

    fun closeQuietly(closeable: Closeable?) {
        if (closeable != null) {
            try {
                closeable.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @Throws(IOException::class)
    fun copy(`in`: InputStream, out: OutputStream) {
        val bytes = ByteArray(4096)
        var count: Int
        try {
            while (`in`.read(bytes).also { count = it } > 0) {
                out.write(bytes, 0, count)
            }
        } finally {
            closeQuietly(`in`)
            closeQuietly(out)
        }
    }

    @Throws(IOException::class)
    fun streamToString(`in`: InputStream): String {
        val len = Math.max(`in`.available(), 1024)
        val out = ByteArrayOutputStream(len)
        copy(`in`, out)
        return String(out.toByteArray(), StandardCharsets.UTF_8)
    }

    @Throws(IOException::class)
    fun getBytes(file: File): ByteArray? {
        if (!file.isFile) {
            return null
        }
        val len = file.length()
        require(len shr 32 == 0L) { "file too large" }
        val out = ByteArrayOutputStream(len.toInt())
        copy(FileInputStream(file), out)
        return out.toByteArray()
    }
}
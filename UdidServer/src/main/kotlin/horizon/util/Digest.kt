package horizon.util

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

object Digest {
    @Throws(NoSuchAlgorithmException::class)
    fun getShortMd5(msg: ByteArray?): String {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(md5(msg))
    }

    @Throws(NoSuchAlgorithmException::class)
    fun md5(msg: ByteArray?): ByteArray {
        return MessageDigest.getInstance("MD5").digest(msg)
    }

    @Throws(NoSuchAlgorithmException::class)
    fun sha1(msg: ByteArray?): ByteArray {
        return MessageDigest.getInstance("SHA-1").digest(msg)
    }

    @Throws(NoSuchAlgorithmException::class)
    fun sha256(msg: ByteArray?): ByteArray {
        return MessageDigest.getInstance("SHA-256").digest(msg)
    }

    @Throws(NoSuchAlgorithmException::class)
    fun sha512(msg: ByteArray?): ByteArray {
        return MessageDigest.getInstance("SHA-512").digest(msg)
    }
}

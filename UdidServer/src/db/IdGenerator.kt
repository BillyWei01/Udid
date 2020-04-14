package db

import config.ConfigManager
import java.util.concurrent.atomic.AtomicLong

/**
 * ID的构造 服务ID+自增序列
 * 最高16bit保留
 * 服务ID有8bit（255)
 * 递增序列有40bit (一万亿）
 *
 * 开发者可以根据情况具体分配
 */
object IdGenerator {
    private const val INIT_SEQ = 10000
    private const val SEQ_BIT = 40
    private const val SEQ_MASK = 0xFFFFFFFFFFL
    private val UPPER_SEQ: Long

    private var UDID_SEQ = AtomicLong(0L)

    init {
        // 划分8bit给服务ID，是为了可以分布式生成ID
        // 如果不需要多台服务器，可以把bit都分给递增序列
        val serverId: Long = ConfigManager.serverConfig.serverId.toLong()
        if (serverId > 255) {
            throw IllegalStateException("server id must less then 255")
        }
        val lower = serverId shl SEQ_BIT
        UPPER_SEQ = lower or SEQ_MASK
        // 查询当前服务最大的序列（服务端ID+递增序列）
        val maxSeq = UdidDao.getMaxSeq(lower, UPPER_SEQ)
        if (maxSeq == 0L) {
            UDID_SEQ.set(lower + INIT_SEQ)
        } else {
            UDID_SEQ.set(maxSeq)
        }
    }

    fun getUdidSeq(): Long {
        val seq =  UDID_SEQ.incrementAndGet()
        if(seq > UPPER_SEQ){
            throw java.lang.IllegalStateException("udid overflow")
        }
        return seq
    }
}

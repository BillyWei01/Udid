package db

import config.ConfigManager
import java.util.concurrent.atomic.AtomicLong

/**
 * ID的构造 服务ID+自增序列
 * 最高8bit保留
 * 服务ID有16bit（65535)
 * 序列有40bit (一万亿）
 *
 * 可以根据情况具体分配（一共64bit）
 */
object IdGenerator {
    private const val INIT_ID = 1L shl 20
    private const val SEQ_BIT = 40
    private const val SEQ_MASK = 0xFFFFFFFFFFL

    private var UDID_SEQ = AtomicLong(0L)

    init {
        val serverId: Long = ConfigManager.serverConfig.serverId.toLong()
        if (serverId >= 65535) {
            throw IllegalStateException("server id must less then 65535")
        }
        val lower = serverId shl SEQ_BIT
        val upper = lower or SEQ_MASK
        val maxSeq = UdidDao.getMaxSeq(lower, upper)
        if (maxSeq == 0L) {
            UDID_SEQ.set(lower + INIT_ID)
        } else {
            UDID_SEQ.set(maxSeq)
        }
    }

    fun getUdidSeq(): Long {
        return UDID_SEQ.incrementAndGet()
    }
}

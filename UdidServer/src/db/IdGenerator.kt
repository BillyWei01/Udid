package db

import config.ConfigManager

/**
 * ID的构造 服务ID+自增序列
 * 最高位(符号位）保留
 * 服务ID有15bit， 取值范围：[0,32767)
 * 序列有40bit(取值范围约一万亿）
 */
object IdGenerator {
    private const val INIT_ID = 1L shl 20
    private const val FLUSH_GAP = 200

    private var udidIncrementCount = 0

    init {
        if(IdSequenceKv.deviceIdSeq == 0L){
            val serverId : Long  = ConfigManager.serverConfig.serverId.toLong()
            if(serverId >= 32767){
                throw IllegalStateException("server id must less then 32767")
            }
            IdSequenceKv.deviceIdSeq =(serverId shl 40) + INIT_ID
        }else{
            // 因为 IdSequenceKv 非阻塞式提交，所以假如宕机的话有可能会丢失更新
            // 所以可以在初始化的时候丢弃 FLUSH_GAP 个ID，以避免ID重复
            // 假设平均每天服务重启5次，则每天会丢弃1000个ID，一年30W+个，相对万亿级别的ID范围，可以忽略不计
            IdSequenceKv.deviceIdSeq += FLUSH_GAP
        }
    }

    @Synchronized
    fun getUdidSeq() : Long{
        val seq = IdSequenceKv.deviceIdSeq + 1
        IdSequenceKv.deviceIdSeq = seq

        // 每新增了一定数量的序列后，调用一次强制提交
        udidIncrementCount++
        if(udidIncrementCount == FLUSH_GAP){
            udidIncrementCount = 0
            IdSequenceKv.data.commit()
        }

        return seq
    }
}
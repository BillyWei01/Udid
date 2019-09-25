package db

import com.horizon.lightkv.KVData
import com.horizon.lightkv.LightKV

/**
 * Demo型服务端，没有Redis数据库
 *
 * 参考：
 * https://github.com/No89757/LightKV
 */
internal object IdSequenceKv :  KVData(){
    override val data: LightKV by lazy {
        // 用的async模式，底层有mmap实现，由操作系统去定时刷新数据到磁盘
        LightKV.Builder("./data/", "id_sequence").async()
    }

    var deviceIdSeq by long(1)
}
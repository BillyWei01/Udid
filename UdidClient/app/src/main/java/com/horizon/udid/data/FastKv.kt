package com.horizon.udid.data

import com.horizon.lightkv.KVData
import com.horizon.lightkv.LightKV
import com.horizon.task.TaskCenter
import com.horizon.udid.application.GlobalConfig
import com.horizon.udid.manager.AppLogger

object FastKv : KVData() {
    override val data: LightKV by lazy {
        // async()模式，利用mmap机制，写入很快
        LightKV.Builder(GlobalConfig.context, "kv_fast")
            .logger(AppLogger)
            .executor(TaskCenter.computation)
            .async()
    }

    var lastSyncUdidTime by long(1)
}
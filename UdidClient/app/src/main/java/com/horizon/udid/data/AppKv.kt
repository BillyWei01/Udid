package com.horizon.udid.data

import com.horizon.lightkv.KVData
import com.horizon.lightkv.LightKV
import com.horizon.task.TaskCenter
import com.horizon.udid.application.GlobalConfig
import com.horizon.udid.manager.AppLogger

object AppKv : KVData() {
    override val data: LightKV by lazy {
        LightKV.Builder(GlobalConfig.context, "kv_safety")
            .logger(AppLogger)
            .executor(TaskCenter.computation)
            .sync()
    }

    var udid by string(1)
    var lastSyncUdidTime by long(2)
}
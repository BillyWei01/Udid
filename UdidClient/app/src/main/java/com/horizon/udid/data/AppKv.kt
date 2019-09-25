package com.horizon.udid.data

import com.horizon.lightkv.KVData
import com.horizon.lightkv.LightKV
import com.horizon.task.TaskCenter
import com.horizon.udid.application.GlobalConfig

object AppKv  : KVData() {
    override val data: LightKV by lazy {
        LightKV.Builder(GlobalConfig.context, "app_kv")
            .executor(TaskCenter.computation)
            .async()
    }

    var udid by long(1)
    var installId by string(2)
    var lastSyncUdidTime by long(3)
}
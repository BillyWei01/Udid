package com.horizon.udid.data

import com.horizon.lightkv.KVData
import com.horizon.lightkv.LightKV
import com.horizon.task.TaskCenter
import com.horizon.udid.application.GlobalConfig
import com.horizon.udid.manager.AppLogger

object SafetyKv : KVData() {
    override val data: LightKV by lazy {
        // sync()模式， 同步写入，双备份，更加安全
        LightKV.Builder(GlobalConfig.context, "kv_safety")
            .logger(AppLogger)
            .executor(TaskCenter.computation)
            .sync()
    }

    var udid by string(1)
    var installId by string(2)
}
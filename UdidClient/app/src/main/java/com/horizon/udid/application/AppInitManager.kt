package com.horizon.udid.application

import com.horizon.udid.manager.AppLogger
import com.horizon.udid.manager.DeviceManager
import io.fastkv.FastKVConfig

object AppInitManager {
    fun appInit() {
        FastKVConfig.setLogger(AppLogger)
        DeviceManager.syncDeviceIdAsync()
    }
}
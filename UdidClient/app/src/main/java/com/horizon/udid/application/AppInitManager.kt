package com.horizon.udid.application

import com.horizon.udid.manager.DeviceManager

object AppInitManager {
    fun appInit(){
        DeviceManager.syncDeviceIdAsync()
    }
}
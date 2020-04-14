package com.horizon.udid.application

import android.app.Application
import com.horizon.udid.data.FastKv
import com.horizon.udid.data.SafetyKv

class AppApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        GlobalConfig.context = this.applicationContext

        // just call data() for loading data async
        SafetyKv.data
        FastKv.data

        AppInitManager.appInit()
    }
}
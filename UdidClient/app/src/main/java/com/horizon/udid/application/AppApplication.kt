package com.horizon.udid.application

import android.app.Application
import com.horizon.udid.data.AppKv

class AppApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        GlobalConfig.context = this.applicationContext

        AppInitManager.appInit()
    }
}
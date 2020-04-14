package com.horizon.udid.manager

import com.horizon.udid.application.GlobalConfig

object PathManager{
    val CACHE_PATH: String = GlobalConfig.context.cacheDir.absolutePath
}
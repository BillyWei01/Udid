package com.horizon.udid.manager

import com.horizon.udid.application.GlobalConfig

object PathManager{
    val CACHE_PATH = GlobalConfig.context.cacheDir.absolutePath
}
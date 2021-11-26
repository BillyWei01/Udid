package com.horizon.udid.application


object PathManager {
    val filesDir: String = GlobalConfig.context.filesDir.absolutePath
    val fastKVDir: String = "$filesDir/fastkv"
}
package com.horizon.udid.manager

import android.util.Log
import com.horizon.udid.BuildConfig
import io.fastkv.FastKV
import java.lang.Exception

object AppLogger : FastKV.Logger {
    override fun i(tag: String, message: String) {
        Log.i(tag, message)
    }

    override fun w(tag: String, e: Exception) {
        Log.w(tag, e)
    }

    override fun e(tag: String, e: Exception) {
        Log.e(tag, e.message, e)
    }

    @JvmStatic
    fun d(tag: String, msg: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, msg)
        }
    }
}
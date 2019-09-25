package com.horizon.udid.manager

import android.util.Log
import com.horizon.udid.BuildConfig

object AppLogger{
    @JvmStatic
    fun e(tag : String, msg : String){
        Log.e(tag, msg)
    }

    @JvmStatic
    fun e(tag : String,t : Throwable){
        Log.e(tag,t.message, t)
    }

    @JvmStatic
    fun i(tag : String, msg : String){
        Log.i(tag, msg)
    }

    @JvmStatic
    fun d(tag : String, msg : String){
        if(BuildConfig.DEBUG){
            Log.d(tag, msg)
        }
    }
}
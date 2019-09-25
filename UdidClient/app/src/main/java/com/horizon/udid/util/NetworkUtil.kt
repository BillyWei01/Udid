package com.horizon.udid.util


import android.content.Context
import android.net.ConnectivityManager
import com.horizon.udid.application.GlobalConfig

object NetworkUtil {
    /**
     * 网络是否连通
     */
    val isConnected: Boolean
        get() {
            val manager = GlobalConfig.context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
            return manager?.activeNetworkInfo?.isConnected ?: false
        }

    /**
     * wifi是否连通
     */
    val isWifiConnected: Boolean
        get() {
            val manager = GlobalConfig.context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
            @Suppress("DEPRECATION")
            return manager?.activeNetworkInfo?.run { isConnected && type == ConnectivityManager.TYPE_WIFI } ?: false
        }
}

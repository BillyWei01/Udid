package com.horizon.udid

import android.annotation.SuppressLint
import android.bluetooth.BluetoothClass.Device
import android.os.Bundle
import com.horizon.did.DeviceId
import com.horizon.udid.application.GlobalConfig
import com.horizon.udid.base.BaseActivity
import com.horizon.udid.data.AppKv
import com.horizon.udid.event.Events
import kotlinx.android.synthetic.main.activity_main.*

/**
 * 客户端需要先配置服务端的IP, 配置到 URLConfig.
 */
class MainActivity : BaseActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (AppKv.localUdid.isEmpty()) {
            AppKv.localUdid = DeviceId.getLocalDevicesId(GlobalConfig.context)
        }
        local_id_tv.text = "local udid: " + AppKv.localUdid

        if (AppKv.serverUdid.isNotEmpty()) {
            server_id_tv.text = "server udid: " + AppKv.serverUdid
        } else {
            server_id_tv.text = "loading server udid..."
        }
    }

    override fun listEvents(): IntArray {
        return intArrayOf(Events.DEVICE_ID_SYNC_COMPLETE)
    }

    @SuppressLint("SetTextI18n")
    override fun onEvent(event: Int, vararg args: Any?) {
        when (event) {
            Events.DEVICE_ID_SYNC_COMPLETE -> {
                val udid = args[0] as String?
                if (udid.isNullOrEmpty()) {
                    server_id_tv.text = "get server udid failed"
                } else {
                    server_id_tv.text = "server udid: $udid"
                }
            }
        }
    }
}


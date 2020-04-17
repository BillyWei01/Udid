package com.horizon.udid

import android.annotation.SuppressLint
import android.os.Bundle
import com.horizon.udid.base.BaseActivity
import com.horizon.udid.data.AppKv
import com.horizon.udid.event.Events
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (AppKv.udid.isNotEmpty()) {
            test_tv.text = "udid: " + AppKv.udid
        } else {
            test_tv.text = "loading udid..."
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
                    test_tv.text = "get udid failed"
                } else {
                    test_tv.text = "get udid: $udid"
                }
            }
        }
    }
}


package com.horizon.udid.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.horizon.event.EventManager
import com.horizon.event.Observer

abstract class BaseActivity : AppCompatActivity(), Observer {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventManager.register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventManager.unregister(this)
    }

    override fun onEvent(event: Int, vararg args: Any?) {
    }

    override fun listEvents(): IntArray {
        return IntArray(0)
    }
}
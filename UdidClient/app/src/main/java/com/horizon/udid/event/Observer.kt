package com.horizon.udid.event

interface Observer {
    fun onEvent(event: Int, vararg args : Any?)
    fun listEvents(): IntArray
}
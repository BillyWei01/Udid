package com.horizon.udid.manager

import com.horizon.did.DeviceId
import com.horizon.did.PhysicsInfo
import com.horizon.udid.application.GlobalConfig
import com.horizon.udid.data.AppKv
import com.horizon.udid.event.EventManager
import com.horizon.udid.event.Events
import com.horizon.udid.network.HttpClient
import com.horizon.udid.network.URLConfig
import utils.HexUtil
import com.horizon.udid.util.NetworkUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

object DeviceManager {
    private const val TAG = "DeviceManager"

    private const val UPDATE_INTERVAL = 0;//2 * 24 * 3600L

    private val JSON_TYPE = MediaType.parse("application/json")

    fun syncDeviceIdAsync() {
        CoroutineScope(Dispatchers.IO).launch {
            syncDeviceId()
        }
    }

    private suspend fun syncDeviceId() {
        if (!NetworkUtil.isConnected) {
            delay(200L)
            EventManager.notify(Events.DEVICE_ID_SYNC_COMPLETE, AppKv.serverUdid)
            return
        }
        val udid = AppKv.serverUdid
        if (udid.isEmpty()) {
            queryUdid()
        } else {
            val now = System.currentTimeMillis()
            if (now - AppKv.lastSyncUdidTime > UPDATE_INTERVAL) {
                AppKv.lastSyncUdidTime = now
                uploadDeviceInfo(udid)
            }
        }
    }

    private fun queryUdid() {
        try {
            val requestJson = getDeviceInfo("query")
            val requestBody = RequestBody.create(JSON_TYPE, requestJson.toString())
            val request = Request.Builder()
                .url(URLConfig.DEVICE_ID_SERVER + "/did")
                .post(requestBody)
                .build()
            val response = HttpClient.request(request)
            AppLogger.i(TAG, response)
            val json = JSONObject(response)
            if (json.getInt("code") == 0) {
                val udid = json.getString("udid")
                AppKv.serverUdid = udid
                EventManager.notify(Events.DEVICE_ID_SYNC_COMPLETE, udid)
                AppKv.lastSyncUdidTime = System.currentTimeMillis()
                return
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, e)
        }
        EventManager.notify(Events.DEVICE_ID_SYNC_COMPLETE, "")
    }

    private fun uploadDeviceInfo(udid: String) {
        try {
            val requestJson = getDeviceInfo("update")
            requestJson.put("udid", udid)
            val requestBody = RequestBody.create(JSON_TYPE, requestJson.toString())
            val request = Request.Builder()
                .url(URLConfig.DEVICE_ID_SERVER + "/did")
                .put(requestBody)
                .build()
            val response = HttpClient.request(request)
            AppLogger.d(TAG, response)
        } catch (e: Exception) {
            AppLogger.e(TAG, e)
        }
    }

    private fun getDeviceInfo(operation: String): JSONObject {
        val context = GlobalConfig.context
        val requestJson = JSONObject()
        val deviceIdInfo = JSONObject().apply {
            put("mac", DeviceId.getMacAddress())
            put("android_id", DeviceId.getAndroidID(context))
            put("serial_no", DeviceId.getSerialNo())
            put("physics_info", HexUtil.long2Hex(PhysicsInfo.getBasicHash(context)))
            put("dark_physics_info", HexUtil.long2Hex(PhysicsInfo.getDarkHash(context)))
        }
        requestJson.put("operation", operation)
        requestJson.put("device_id_info", deviceIdInfo)
        return requestJson
    }




}
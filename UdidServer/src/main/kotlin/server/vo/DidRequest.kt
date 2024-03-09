package server.vo

import com.alibaba.fastjson2.annotation.JSONField

data class DidRequest(
    @JSONField(name = "operation")
    val operation: String,

    @JSONField(name = "device_id_info")
    val deviceIdInfo: DeviceIdInfo,

    @JSONField(name = "udid")
    val udid: String? = null
)

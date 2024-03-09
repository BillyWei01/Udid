package server.vo

import com.alibaba.fastjson2.annotation.JSONField


data class DeviceIdInfo (
    @JSONField(name = "android_id")
    val androidId: String,

    @JSONField(name = "widevine_id")
    val widevineId: String,

    @JSONField(name = "device_hash")
    val deviceHash: String,

    @JSONField(name = "local_did")
    val localDid: String
)

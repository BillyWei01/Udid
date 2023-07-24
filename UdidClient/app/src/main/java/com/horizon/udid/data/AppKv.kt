package com.horizon.udid.data

object AppKv : KVData("app_kv") {
    var localUdid by string("local_udid")
    var serverUdid by string("uuid")
    var lastSyncUdidTime by long("last_sync_udid_time")
}
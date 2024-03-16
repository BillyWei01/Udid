package server.db.bean

data class DeviceId (
    var seq: Long = 0,
    var udid: Long = 0,

    var createTime: Long = 0,
    var updateTime: Long = 0,

    var androidId: Long = 0,
    var widevineId: Long = 0,
    var deviceHash: Long = 0,

    // local_did可用于辅助跟踪设备ID变更，但本项目不体现这一点，大家可自行选择
    var localDid: String = "",
)

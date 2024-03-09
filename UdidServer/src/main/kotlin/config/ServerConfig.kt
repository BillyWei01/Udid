package config

import com.alibaba.fastjson2.annotation.JSONField

class ServerConfig {
    @JSONField(name = "server_id")
    var serverId = 0
}

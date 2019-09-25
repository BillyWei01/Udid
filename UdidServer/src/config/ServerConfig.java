package config;

import com.alibaba.fastjson.annotation.JSONField;

public class ServerConfig {
    @JSONField(name = "server_id")
    private int serverId;

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int server_id) {
        this.serverId = server_id;
    }
}

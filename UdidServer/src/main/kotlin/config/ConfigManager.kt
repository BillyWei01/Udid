package config

import com.alibaba.fastjson2.JSON
import horizon.util.IOUtil
import java.io.BufferedInputStream
import java.io.FileInputStream

object ConfigManager {
    val serverConfig: ServerConfig
        get() {
            val path = "./config/server_config.txt"
            val inputStream = BufferedInputStream(FileInputStream(path))
            return IOUtil.streamToString(inputStream).let {
                JSON.parseObject(it, ServerConfig::class.java)
            }
        }

}

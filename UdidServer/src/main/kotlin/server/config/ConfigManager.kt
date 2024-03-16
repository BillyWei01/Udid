package server.config

import com.alibaba.fastjson2.JSON
import tools.util.IOUtil
import java.io.BufferedInputStream
import java.io.FileInputStream

object ConfigManager {
    val serverConfig: ServerConfig by lazy {
        val path = "./config/server_config.txt"
        val inputStream = BufferedInputStream(FileInputStream(path))
        IOUtil.streamToString(inputStream).let {
            JSON.parseObject(it, ServerConfig::class.java)
        }
    }
}

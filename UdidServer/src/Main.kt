import horizon.task.TaskCenter
import com.sun.net.httpserver.HttpServer
import config.ConfigManager
import server.UdidHandler
import java.net.InetSocketAddress

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        init()

        val address = InetSocketAddress(8080)
        val server = HttpServer.create(address, 0)
        server.createContext(UdidHandler.PATH, UdidHandler())
        server.executor = TaskCenter.executor
        server.start()
        println("Server is listening on port: " + address.port)
    }

    private fun init() {
        val serverId = ConfigManager.serverConfig.serverId
        println("server $serverId start running")
    }
}

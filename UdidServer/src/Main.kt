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
        server.executor = TaskCenter.io
        server.start()
        println("Server is listening on port 8080")
    }

    private fun init() {
        val serverId = ConfigManager.serverConfig.serverId
        System.out.println("server $serverId start running")

    }
}

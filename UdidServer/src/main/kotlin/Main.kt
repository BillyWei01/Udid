import com.sun.net.httpserver.HttpServer
import config.ConfigManager
import horizon.task.TaskCenter
import server.UdidHandler
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.SocketException


fun main(args: Array<String>) {
    init()

    val address = InetSocketAddress(8080)
    val server = HttpServer.create(address, 0)
    server.createContext(UdidHandler.PATH, UdidHandler())
    server.executor = TaskCenter.executor

    server.start()

    println("Server is listening on ${getIp()}:${address.port}")
}

/**
 * 获取IP
 */
private fun getIp(): String {
    try {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
            val networkInterface = interfaces.nextElement()
            if (networkInterface.isLoopback || networkInterface.isVirtual || !networkInterface.isUp) {
                continue
            }
            val addresses = networkInterface.inetAddresses
            while (addresses.hasMoreElements()) {
                val inetAddress = addresses.nextElement()
                if (inetAddress.isSiteLocalAddress) {
                    // Local LAN IP Address
                    return inetAddress.hostAddress
                }
            }
        }
    } catch (e: SocketException) {
        e.printStackTrace()
    }
    return ""
}

private fun init() {
    val serverId = ConfigManager.serverConfig.serverId
    println("server $serverId start running")
}
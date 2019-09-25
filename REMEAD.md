## UDID

本项目提供一种Android移动设备构造UDID的方案。
项目分为两个目录，UdidClient和UdidServer。

## 使用方法

客户端和服务端分别在Android Studio和Intellij IDEA上开发，用对应的IDE上运行即可。

服务端代码，服务监听用的JDK自带的HttpServer，数据库用的sqlite-jdbc, 所以服务端可以零配置运行。

客户端代码，需要先修改URLConfig的地址:

```kotlin
object URLConfig{
    const val DEVICE_ID_SERVER = "http://192.168.0.113:8080"
}
```

然后服务端如果是在PC上运行，客户和服务端需要同一网段。


## License
See the [LICENSE](LICENSE.md) file for license rights and limitations.
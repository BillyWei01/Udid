package com.horizon.udid.network

import com.horizon.udid.exception.ServerException
import com.horizon.udid.manager.PathManager
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.lang.Exception
import java.util.concurrent.TimeUnit

object HttpClient {
    private val client : OkHttpClient by lazy {
        OkHttpClient.Builder()
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .connectTimeout(20, TimeUnit.SECONDS)
            .cache(Cache(File(PathManager.CACHE_PATH, "http"), (128L shl 20)))
            .build()
    }

    fun request(request : Request) : String{
        val response = client.newCall(request).execute()
        val body = response.body()
        if (response.isSuccessful) {
            if (body != null) {
                return body.string()
            }
        }else{
            var msg = ""
            if (body != null) {
                try {
                    val json = JSONObject(body.string())
                    msg = json.optString("message", "")
                }catch (ignore : Exception){
                }
                if(msg.isEmpty()){
                    msg = "服务器出错，响应码：" + response.code()
                }
            }
            throw  ServerException(msg)
        }
        return ""
    }
}
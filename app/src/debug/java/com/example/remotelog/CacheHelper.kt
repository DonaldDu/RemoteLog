package com.example.remotelog

import com.dhy.remotelog.RemoteCacheHelper
import com.dhy.remotelog.requestInfo
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class CacheHelper : RemoteCacheHelper {
    companion object {
        var userCache = false
        var testId = "f639de62-256d-4e88-b114-3eab04b18abc"

        /**
         * add before logInterceptor
         * */
        fun appendInfo(builder: OkHttpClient.Builder) {
            builder.addInterceptor {
                val req = it.request().newBuilder().addHeader("testId", testId).build()
                it.proceed(req)
            }
        }
    }

    override fun userCache(req: Request): Boolean {
        return userCache
    }

    override fun buildFilter(appId: String, req: Request): String? {
        val info = req.requestInfo
        return if (info != null) {
            val json = JSONObject()
            json.putOpt("appId", appId)
            json.putOpt("cmd", info.cmd)
            json.putOpt("headers.testId", testId)
            json.toString()
        } else null
    }
}
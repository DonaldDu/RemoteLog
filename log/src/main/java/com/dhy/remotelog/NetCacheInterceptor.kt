package com.dhy.remotelog

import com.dhy.remotelog.RemoteLog.Companion.URL_XLOG
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject

/**
 * must be add after LogInterceptor
 * */
class NetCacheInterceptor(val userCache: (Request) -> Boolean, private val packageName: String, private val X_LC_ID: String, private val X_LC_KEY: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val rawReq = chain.request()
        if (userCache(rawReq)) {
            val old = chain.request()
            val where = buildWhere(old)
            if (where != null) {
                val req = old.newBuilder()
                    .header(LogInterceptor.HEADER_IGNORE_LOG, LogInterceptor.HEADER_IGNORE_LOG)
                    .url("$URL_XLOG?limit=1&where=$where")
                    .loadIdKey()
                    .build()
                val res = chain.proceed(req)
                val body = res.peekRealResponseBody()
                return if (body != null) res.newBuilder().body(body).build()
                else chain.proceed(rawReq)
            }
        }
        return chain.proceed(rawReq)
    }

    private fun Response.peekRealResponseBody(): ResponseBody? {
        val json = copyResponse()
        if (json != null) {
            try {
                val obj = JSONObject(json)
                val logs = obj.getJSONArray("results") as JSONArray
                if (logs.length() > 0) {
                    val log = logs[0] as JSONObject
                    return log.getJSONObject("response")
                        .toString()
                        .toResponseBody(null)
                }
            } catch (e: Exception) {
                println("bufferInterceptor error")
                e.printStackTrace()
            }
        }
        return null
    }

    private fun buildWhere(req: Request): String? {
        val info = req.tag(RequestInfo::class.java)
        return if (info != null) {
            val json = JSONObject()
            json.putOpt("appId", packageName)
            json.putOpt("path", info.path)
            json.putOpt("unique", info.unique)
            json.toString()
        } else null
    }

    private fun Request.Builder.loadIdKey(): Request.Builder {
        header("X-LC-Id", X_LC_ID)
        header("X-LC-Key", X_LC_KEY)
        return this
    }
}
package com.dhy.remotelog

import android.content.Context
import com.dhy.remotelog.RemoteLog.Companion.URL_XLOG
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

open class RemoteLog(private val appId: String, private val debug: Boolean, private val initRequest: (Request.Builder, RequestBody) -> Unit) : ILogDataWriter {
    companion object {
        var user: String = ""
        internal const val HEADER_CMD = "cmd"
        internal const val URL_XLOG = "https://api.leancloud.cn/1.1/classes/XLog"
    }

    var enqueue: Boolean = true
    private val client = OkHttpClient()

    private val JSON_MEDIA_TYPE = "application/json; charset=UTF-8".toMediaTypeOrNull()!!

    private val callback = object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
        }

        @Throws(IOException::class)
        override fun onResponse(call: Call, response: Response) {
            onGetResponse(response)
        }
    }

    override fun write(requestInfo: RequestInfo, response: String, httpCode: Int, costTimeMS: Long) {
        val obj = JSONObject()
        try {
            initData(obj, requestInfo, response)
            obj.put("status", httpCode)
            obj.put("costTimeMS", costTimeMS)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val requestBody = obj.toString().toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder().apply { initRequest(this, requestBody) }.build()
        if (enqueue) {
            client.newCall(request).enqueue(callback)
        } else {
            try {
                onGetResponse(client.newCall(request).execute())
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    @Throws(JSONException::class)
    private fun initData(obj: JSONObject, request: RequestInfo, response: String) {
        obj.put("user", user)
        obj.put("appId", appId)
        if (request.headers != null) {
            val cmd = request.headers.remove(HEADER_CMD)
            if (cmd != null) obj.put(HEADER_CMD, cmd)
            obj.put("headers", JSONObject(request.headers))
        }
        obj.put("unique", request.unique)
        obj.put("method", request.method)
        obj.put("server", request.server)
        obj.put("path", request.path)

        val params = JSONObject()
        if (request.query != null) {
            params.put("query", JSONObject(request.query))
        }
        if (request.forms != null) {
            params.put("forms", JSONObject(request.forms))
        }
        if (request.json != null) {
            params.put("json", parse(request.json))
        }
        obj.put("params", params)
        obj.put("response", parse(response))
        loadExtra(obj, request.extraLog)
    }

    private fun parse(str: String): JSONObject {
        return when {
            str.startsWith("{") -> JSONObject(str)
            str.startsWith("[") -> {
                JSONObject().apply {
                    put("it", JSONArray(str))
                }
            }
            else -> {
                JSONObject().apply {
                    put("it", str)
                }
            }
        }
    }

    @Throws(JSONException::class)
    private fun loadExtra(obj: JSONObject, extraLog: IExtraLog?) {
        if (extraLog?.extra != null) {
            val extra = extraLog.extra!!
            for (key in extra.keys) {
                obj.put(key, extra[key])
            }
        }
    }

    @Throws(IOException::class)
    protected open fun onGetResponse(response: Response) {
        if (debug) {
            val body = response.body()
            if (body != null) {
                println(body.string())
                body.close()
            }
        }
    }
}

fun OkHttpClient.Builder.initRemoteLog(context: Context, userCache: ((Request) -> Boolean)? = null) {
    val appId = context.applicationInfo.packageName
    val X_LC_ID = context.getString(R.string.X_LC_ID)
    val X_LC_KEY = context.getString(R.string.X_LC_KEY)
    if (X_LC_ID.isInvalidResValue() || X_LC_KEY.isInvalidResValue()) return
    val logWriter = RemoteLog(appId, false) { qb, body ->
        qb.url(URL_XLOG)
        qb.header("Content-Type", "application/json")
        qb.header("X-LC-Id", X_LC_ID)
        qb.header("X-LC-Key", X_LC_KEY)
        qb.post(body)
    }
    addInterceptor(LogInterceptor(logWriter))
    if (userCache != null) addInterceptor(NetCacheInterceptor(userCache, appId, X_LC_ID, X_LC_KEY))
}

private fun String.isInvalidResValue(): Boolean {
    return this.isEmpty() || this == "null";
}
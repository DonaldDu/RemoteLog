package com.dhy.remotelog

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

open class RemoteLog(private val appId: String, private val debug: Boolean, private val initRequest: (Request.Builder, RequestBody) -> Unit) : ILogDataWriter {
    companion object {
        var user: String = ""
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
            obj.put("headers", JSONObject(request.headers))
        }
        obj.put("method", request.method)
        obj.put("server", request.server)
        obj.put("path", request.path)

        if (request.query != null) {
            obj.put("query", JSONObject(request.query))
        }
        if (request.forms != null) {
            obj.put("forms", JSONObject(request.forms))
        }
        if (request.json != null) {
            if (request.json.startsWith("[")) {
                val array = JSONObject()
                array.put("it", JSONArray(request.json))
                obj.put("json", array)
            } else {
                obj.put("json", JSONObject(request.json))
            }
        }
        obj.put("unique", request.unique)
        obj.put("response", response)
        loadExtra(obj, request.extraLog)
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
            val body = response.body
            if (body != null) {
                println(body.string())
                body.close()
            }
        }
    }
}

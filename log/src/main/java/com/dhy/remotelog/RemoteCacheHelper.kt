package com.dhy.remotelog

import okhttp3.Request
import org.json.JSONObject

interface RemoteCacheHelper {
    fun userCache(req: Request): Boolean
    fun buildFilter(appId: String, req: Request): String? {
        val info = req.requestInfo
        return if (info != null) {
            val json = JSONObject()
            json.putOpt("appId", appId)
            json.putOpt("unique", info.unique)
            json.toString()
        } else null
    }
}
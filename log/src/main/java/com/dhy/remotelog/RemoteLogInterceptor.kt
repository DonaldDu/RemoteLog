package com.dhy.remotelog

import com.google.auto.service.AutoService
import okhttp3.Interceptor
import okhttp3.Response

@AutoService(Interceptor::class)
class RemoteLogInterceptor : Interceptor {
    companion object {
        @JvmStatic
        internal var insert: ((RequestLog) -> Unit)? = null
    }

    private val logDataWriter = object : ILogDataWriter {
        override fun write(requestInfo: RequestInfo, response: String, httpCode: Int, costTimeMS: Long) {
            if (insert != null) {
                val log = requestInfo.toLog(response, httpCode, costTimeMS)
                insert!!(log)
            }
        }
    }
    private val logInterceptor = LogInterceptor(logDataWriter)
    override fun intercept(chain: Interceptor.Chain): Response {
        return logInterceptor.intercept(chain)
    }
}
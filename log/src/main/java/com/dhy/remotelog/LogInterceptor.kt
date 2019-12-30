package com.dhy.remotelog


import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class LogInterceptor(private val writer: ILogDataWriter) : Interceptor {
    companion object {
        const val HEADER_IGNORE_LOG = "HEADER_IGNORE_LOG"
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val requestInfo = RequestInfo(request)
        request = request.newBuilder()
            .tag(RequestInfo::class.java, requestInfo)
            .build()
        val time = System.currentTimeMillis()
        try {
            val response = chain.proceed(request)
            val cost = System.currentTimeMillis() - time
            if (response.shouldLog()) writer.write(requestInfo, copyResponse(response), response.code, cost)
            return response
        } catch (e: Exception) {
            val cost = System.currentTimeMillis() - time
            writer.write(requestInfo, "local error, ${e.javaClass.name}: ${e.message}", -1, cost)
            throw e
        }
    }

    private fun Response.shouldLog(): Boolean {
        return request.header(HEADER_IGNORE_LOG) == null
    }

    private val contentLength: Long = 1024 * 1024  //1MB
    @Throws(IOException::class)
    private fun copyResponse(response: Response): String {
        val data: String
        if (response.body != null) {
            val responseBody = response.peekBody(contentLength)
            data = responseBody.string()
            responseBody.close()
        } else data = "{}"
        return data
    }
}

fun Response.copyResponse(): String? {
    val data: String?
    if (body != null) {
        val responseBody = peekBody(1024 * 1024)
        data = responseBody.string()
        responseBody.close()
    } else data = null
    return data
}

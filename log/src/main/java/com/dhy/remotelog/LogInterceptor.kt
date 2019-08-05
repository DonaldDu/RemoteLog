package com.dhy.remotelog


import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class LogInterceptor(private val writer: ILogDataWriter) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val requestInfo = RequestInfo(request)
        request = request.newBuilder().tag(requestInfo).build()
        val time = System.currentTimeMillis()
        try {
            val response = chain.proceed(request)
            val cost = System.currentTimeMillis() - time
            writer.write(requestInfo, copyResponse(response), response.code, cost)
            return response
        } catch (e: Exception) {
            val cost = System.currentTimeMillis() - time
            writer.write(requestInfo, "local error, ${e.javaClass.name}: ${e.message}", -1, cost)
            throw e
        }
    }

    private val contentLength = (128 * 1024).toLong() //128kb
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

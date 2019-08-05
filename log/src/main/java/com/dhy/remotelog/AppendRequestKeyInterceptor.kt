package com.dhy.remotelog

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class AppendRequestKeyInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        if (RequestInfo.isNotGet(request)) {
            val httpRequest = RequestInfo(request)
            request = httpRequest.appendRequestKey(request)
        }
        return chain.proceed(request)
    }
}

package com.dhy.remotelog

import okhttp3.Response

interface IDataHandler {
    /**
     * save for mocking
     */
    fun write(httpRequest: RequestInfo, data: String)


    fun mock(httpRequest: RequestInfo): Response
}

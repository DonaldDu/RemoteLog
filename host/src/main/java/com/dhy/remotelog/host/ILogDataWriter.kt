package com.dhy.remotelog.host

interface ILogDataWriter {
    fun write(requestInfo: RequestInfo, response: String, httpCode: Int, costTimeMS: Long)
}

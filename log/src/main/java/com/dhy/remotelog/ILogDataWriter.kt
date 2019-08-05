package com.dhy.remotelog

interface ILogDataWriter {
    fun write(requestInfo: RequestInfo, response: String, httpCode: Int, costTimeMS: Long)
}

package com.dhy.remotelog

import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okio.ByteString

fun String.toResponseBody(contentType: MediaType? = null): ResponseBody {
    return ResponseBody.create(contentType, this)
}

fun String.toRequestBody(contentType: MediaType? = null): RequestBody {
    return RequestBody.create(contentType, this)
}

fun String.toMediaTypeOrNull(): MediaType? {
    return MediaType.parse(this)
}

fun String.encodeUtf8(): ByteString {
    return ByteString.encodeUtf8(this)
}
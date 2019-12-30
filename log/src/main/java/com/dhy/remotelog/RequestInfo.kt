package com.dhy.remotelog

import com.dhy.remotelog.RemoteLog.Companion.HEADER_CMD
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okio.Buffer
import okio.ByteString.Companion.encodeUtf8
import java.io.IOException
import java.nio.charset.Charset
import java.util.*

class RequestInfo(request: Request) {
    val method: String
    /**
     * host&port
     */
    val server: String
    val path: String

    val headers: MutableMap<String, String>?
    val query: Map<String, String>?
    val forms: Map<String, String>?
    val json: String?
    val extraLog: IExtraLog?
    /**
     * the key of request create with all parameters's MD5
     */
    val unique: String
    val cmd: String?
    private val UTF_8 = Charset.forName("UTF-8")

    init {
        val url = request.url
        this.method = request.method
        this.server = initServer(url)
        this.path = url.encodedPath
        this.headers = initHeaders(request)
        this.query = initQuery(url)
        this.forms = initForms(request)
        this.json = initJson(request)
        this.unique = initUnique()
        this.cmd = request.header(HEADER_CMD)
        this.extraLog = initIExtraLog(request)
    }

    fun appendRequestKey(request: Request): Request {
        val url = request.url.newBuilder().addQueryParameter("unique", unique).build()
        return request.newBuilder().url(url).build()
    }

    private fun initIExtraLog(request: Request): IExtraLog? {
        val tag = request.tag()
        return if (tag is IExtraLog) tag else null
    }

    private fun initHeaders(request: Request): MutableMap<String, String>? {
        val headers = request.headers
        val size = headers.size
        if (size > 0) {
            val map = HashMap<String, String>()
            for (i in 0 until size) {
                map[headers.name(i)] = headers.value(i)
            }
            return map
        }
        return null
    }

    private fun initQuery(url: HttpUrl): Map<String, String>? {
        val size = url.querySize
        if (size > 0) {
            val map = HashMap<String, String>()
            for (i in 0 until size) {
                map[url.queryParameterName(i)] = url.queryParameterValue(i) ?: ""
            }
            return map
        }
        return null
    }

    private fun initServer(url: HttpUrl): String {
        val head: String
        var port = ""
        if (url.isHttps) {
            head = "https://"
            if (url.port != 443) port = ":" + url.port
        } else {
            head = "http://"
            if (url.port != 80) port = ":" + url.port
        }
        return head + url.host + port
    }

    private fun initForms(request: Request): Map<String, String>? {
        val body = request.body
        if (body is FormBody) {
            return initFromsFromFormBody((body as FormBody?)!!)
        } else if (body is MultipartBody) {
            return initFromsFromMultipartBody((body as MultipartBody?)!!)
        }
        return null
    }

    private fun initFromsFromFormBody(formBody: FormBody): Map<String, String>? {
        val size = formBody.size
        if (size > 0) {
            val forms = HashMap<String, String>()
            for (i in 0 until size) {
                forms[formBody.name(i)] = formBody.value(i)
            }
            return forms
        }
        return null
    }

    private fun initFromsFromMultipartBody(multipartBody: MultipartBody): Map<String, String> {
        val size = multipartBody.size
        val CONTENT_DISPOSITION = "Content-Disposition"
        var name: String
        var value: String
        val forms = HashMap<String, String>()
        for (i in 0 until size) {
            val part = multipartBody.part(i)
            val cd = part.headers!![CONTENT_DISPOSITION]
            val start = "form-data; name=".length
            var end = cd!!.indexOf(";", start)
            if (end == -1) end = cd.length
            name = cd.substring(start + 1, end - 1)

            val body = part.body
            value = if (FILE_MEDIA_TYPES.contains(body.contentType())) {
                "MEDIA_TYPE_FILE"
            } else {
                readRequestBody(body)
            }
            forms[name] = value
        }
        return forms
    }

    private fun readRequestBody(body: RequestBody): String {
        val buffer = Buffer()
        return try {
            body.writeTo(buffer)
            buffer.readString(UTF_8)
        } catch (e: IOException) {
            ""
        } finally {
            buffer.clone()
        }
    }

    private fun initJson(request: Request): String? {
        val body = request.body
        if (body != null) {
            val mediaType = body.contentType()
            if (JSON_MEDIA_TYPES.contains(mediaType)) {
                return readRequestBody(body)
            }
        }
        return null
    }

    private fun initUnique(): String {
        val sb = StringBuilder()
        sb.append(method).append(server).append(path)
        if (query != null) sb.append(gson.toJson(query))
        if (forms != null) sb.append(gson.toJson(forms))
        if (json != null) sb.append(json)
        return sb.toString().encodeUtf8().md5().hex()
    }

    companion object {
        @Transient
        private val gson = Gson()
        @Transient
        private val JSON_MEDIA_TYPES: MutableList<MediaType>
        @Transient
        private val FILE_MEDIA_TYPES: MutableList<MediaType>

        init {
            JSON_MEDIA_TYPES = ArrayList()
            JSON_MEDIA_TYPES.add("application/json; charset=UTF-8".toMediaTypeOrNull()!!)
            JSON_MEDIA_TYPES.add("application/json; charset=utf-8".toMediaTypeOrNull()!!)

            FILE_MEDIA_TYPES = ArrayList()
            FILE_MEDIA_TYPES.add("application/octet-stream".toMediaTypeOrNull()!!)
        }

        fun isNotGet(request: Request): Boolean {
            return request.method != "GET"
        }
    }
}

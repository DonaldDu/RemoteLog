package com.dhy.remotelog

import com.google.gson.Gson
import okhttp3.*
import okio.Buffer
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Invocation
import retrofit2.http.*
import java.io.IOException
import java.nio.charset.Charset

class RequestInfo(request: Request) {
    val method: String

    /**
     * host&port
     */
    val server: String
    val path: String

    val headers: JSONObject?
    private val query: JSONObject?
    private val forms: JSONObject?
    private val json: String?
    val extraLog: IExtraLog?

    /**
     * the key of request create with all parameters's MD5
     */
    val unique: String
    val cmd: String?

    /**
     * include query and body params
     * */
    val params = JSONObject()
    private val UTF_8 = Charset.forName("UTF-8")

    init {
        val url = request.url()
        this.method = request.method()
        this.server = initServer(url)
        this.path = url.encodedPath()
        this.headers = initHeaders(request)
        this.query = initQuery(url)
        this.forms = initForms(request)
        this.json = initJson(request)
        this.cmd = request.getCmd()
        this.extraLog = initIExtraLog(request)

        if (query != null) {
            params.put("query", query)
        }
        if (forms != null) {
            params.put("forms", forms)
        }
        if (json != null) {
            if (json.startsWith("{")) {
                params.put("json", JSONObject(json))
            } else {
                params.put("json", JSONArray(json))
            }
        }
        this.unique = initUnique()
    }

    fun appendRequestKey(request: Request): Request {
        val url = request.url().newBuilder().addQueryParameter("unique", unique).build()
        return request.newBuilder().url(url).build()
    }

    private fun Request.getCmd(): String? {
        val invocation = tag(Invocation::class.java) ?: return null
        val m = invocation.method()
        val get = m.getAnnotation(GET::class.java)
        if (get != null) return get.value

        val post = m.getAnnotation(POST::class.java)
        if (post != null) return post.value

        val put = m.getAnnotation(PUT::class.java)
        if (put != null) return put.value

        val delete = m.getAnnotation(DELETE::class.java)
        if (delete != null) return delete.value

        val patch = m.getAnnotation(PATCH::class.java)
        if (patch != null) return patch.value
        return null
    }

    private fun initIExtraLog(request: Request): IExtraLog? {
        val tag = request.tag()
        return if (tag is IExtraLog) tag else null
    }

    private fun initHeaders(request: Request): JSONObject? {
        val headers = request.headers()
        val size = headers.size()
        if (size > 0) {
            val map = JSONObject()
            for (i in 0 until size) {
                map.put(headers.name(i), headers.value(i))
            }
            return map
        }
        return null
    }

    private fun initQuery(url: HttpUrl): JSONObject? {
        val size = url.querySize()
        if (size > 0) {
            val map = JSONObject()
            for (i in 0 until size) {
                map.put(url.queryParameterName(i), url.queryParameterValue(i) ?: "")
            }
            return map
        }
        return null
    }

    private fun initServer(url: HttpUrl): String {
        val head: String = url.scheme() + "://"
        return if (url.port() != HttpUrl.defaultPort(url.scheme())) {
            head + url.host() + ":" + url.port()
        } else head + url.host()
    }

    private fun initForms(request: Request): JSONObject? {
        val body = request.body()
        if (body is FormBody) {
            return initFromsFromFormBody((body as FormBody?)!!)
        } else if (body is MultipartBody) {
            return initFromsFromMultipartBody((body as MultipartBody?)!!)
        }
        return null
    }

    private fun initFromsFromFormBody(formBody: FormBody): JSONObject? {
        val size = formBody.size()
        if (size > 0) {
            val forms = JSONObject()
            for (i in 0 until size) {
                val name = formBody.name(i)
                val value = if (name.contains("file")) "MEDIA_TYPE_FILE" else formBody.value(i)
                forms.put(name, value)
            }
            return forms
        }
        return null
    }

    private fun initFromsFromMultipartBody(multipartBody: MultipartBody): JSONObject {
        val size = multipartBody.size()
        val CONTENT_DISPOSITION = "Content-Disposition"
        var name: String
        var value: String
        val forms = JSONObject()
        for (i in 0 until size) {
            val part = multipartBody.part(i)
            val cd = part.headers()!![CONTENT_DISPOSITION]
            val start = "form-data; name=".length
            var end = cd!!.indexOf(";", start)
            if (end == -1) end = cd.length
            name = cd.substring(start + 1, end - 1)

            val body = part.body()
            value = if (name.contains("file")) {
                "MEDIA_TYPE_FILE"
            } else {
                readRequestBody(body)
            }
            forms.put(name, value)
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
        val body = request.body()
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

        init {
            JSON_MEDIA_TYPES = ArrayList()
            JSON_MEDIA_TYPES.add("application/json; charset=UTF-8".toMediaTypeOrNull()!!)
            JSON_MEDIA_TYPES.add("application/json; charset=utf-8".toMediaTypeOrNull()!!)
        }

        fun isNotGet(request: Request): Boolean {
            return request.method() != "GET"
        }
    }
}

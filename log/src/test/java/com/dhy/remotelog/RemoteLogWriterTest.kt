package com.dhy.remotelog

import io.reactivex.Single
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.POST
import retrofit2.http.Query

class RemoteLogWriterTest {
    private var api: API? = null

    @Before
    fun setUp() {
        val writer = RemoteLog("com.wwgps.ect", true) { builder, body ->
            builder.header("Content-Type", "application/json")
            builder.url("https://api.leancloud.cn/1.1/classes/XLog2")
            builder.header("X-LC-Id", "EPEKEOSj0jor30zrX6ybm6qS-gzGzoHsz")
            builder.header("X-LC-Key", BuildConfig.XLOG_MASTER_KEY)//Master Key
            builder.post(body)
        }
        writer.enqueue = false
        val builder = OkHttpClient().newBuilder()
            .addInterceptor(LogInterceptor(writer))
            .addInterceptor(AppendRequestKeyInterceptor())

        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .baseUrl("http://test3.wwvas.com:9999")
            .client(builder.build())
            .build()
        api = retrofit.create(API::class.java)
    }

    @Test
    fun testLogin() {
        api!!.login("1", "0")
            .subscribe(
                { token -> println(token) },
                { throwable -> throwable.printStackTrace() }
            )
    }

    interface API {
        @POST("auth/token")
        fun login(@Query("username") userName: String, @Query("password") password: String): Single<String>
    }
}

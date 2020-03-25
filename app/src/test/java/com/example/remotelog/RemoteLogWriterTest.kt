package com.example.remotelog

import com.dhy.remotelog.LogInterceptor
import com.dhy.remotelog.RemoteLog
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
            builder.header("X-LC-Id", BuildConfig.X_LC_ID)
            builder.header("X-LC-Key", BuildConfig.X_LC_KEY)
            builder.post(body)
        }
        writer.enqueue = false
        val builder = OkHttpClient().newBuilder()
            .addInterceptor(LogInterceptor(writer))

        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .baseUrl("http://www.demo.com")
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

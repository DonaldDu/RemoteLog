package com.example.remotelog

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dhy.remotelog.host.NetLogActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var api: APIs
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()

        buttonTest.setOnClickListener {
            api.fetchHTML(false)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Toast.makeText(this, "response length " + it.msg, Toast.LENGTH_LONG).show()
                }, {
                    Toast.makeText(this, "response error " + it.message, Toast.LENGTH_LONG).show()
                })
        }
        btShowLog.setOnClickListener {
            startActivity(Intent(this, NetLogActivity::class.java))
        }
    }

    private fun init() {
        val client = OkHttpClient.Builder()
            .apply {
//                if (BuildConfig.DEBUG) {
//                    initRemoteLog(this@MainActivity) {
//                        useBuffer.isChecked
//                    }
//                }
                addInterceptor(Interceptor { chain ->
                    val req = chain.request().newBuilder().header("_USER_", "10086").build()//fixme add user to head
                    chain.proceed(req)
                })
                if (BuildConfig.DEBUG) addInterceptorWithServiceLoader()
            }.build()

        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .baseUrl("http://www.demo.com/")
            .client(client)
            .build()
        api = retrofit.create(APIs::class.java)
    }

    private fun OkHttpClient.Builder.addInterceptorWithServiceLoader() {
        val iterator = ServiceLoader.load(Interceptor::class.java).iterator()
        while (iterator.hasNext()) {
            addInterceptor(iterator.next())
        }
    }

    interface APIs {
        @GET("https://www.wwvas.com:9101/vasms-ect/admin/versioninfo/get?packagename=com.wwgps.puche&versiontype=6")
        fun fetchHTML(@Query("unused") unused: Boolean): Observable<Response<AppVersion>>
    }
}

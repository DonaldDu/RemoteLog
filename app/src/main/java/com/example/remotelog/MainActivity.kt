package com.example.remotelog

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dhy.remotelog.NetLogActivity
import com.dhy.remotelog.RemoteLog
import com.dhy.remotelog.initRemoteLog
import com.dhy.remotelog.requestInfo
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

class MainActivity : AppCompatActivity() {
    private lateinit var api: APIs

    @SuppressLint("CheckResult")
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
        RemoteLog.user = "10086"
        val client = OkHttpClient.Builder()
            .apply {
                if (BuildConfig.DEBUG) {
                    initRemoteLog(applicationContext) {
                        useBuffer.isChecked
                    }
                }
                addInterceptor(Interceptor { chain ->
                    val req = chain.request()
                    Log.i("requestInfo", req.url.toString() + req.requestInfo?.params.toString())
                    chain.proceed(req)
                })
            }.build()

        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .baseUrl("http://www.demo.com/")
            .client(client)
            .build()
        api = retrofit.create(APIs::class.java)
    }

    interface APIs {
        @GET("https://www.wwvas.com:9101/vasms-ect/admin/versioninfo/get?packagename=com.wwgps.puche&versiontype=6")
        fun fetchHTML(@Query("unused") unused: Boolean): Observable<Response<AppVersion>>
    }
}

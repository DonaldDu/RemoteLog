package com.example.remotelog

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dhy.remotelog.initRemoteLog
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class MainActivity : AppCompatActivity() {
    lateinit var api: APIs
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
    }

    private fun init() {
        val client = OkHttpClient.Builder()
            .apply {
                if (BuildConfig.DEBUG) {
                    initRemoteLog(this@MainActivity) {
                        useBuffer.isChecked
                    }
                }
            }.build()

        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
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

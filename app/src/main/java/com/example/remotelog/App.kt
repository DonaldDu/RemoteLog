package com.example.remotelog

import android.app.Application
import com.dhy.remotelog.NetLogKit
import com.didichuxing.doraemonkit.DoKit

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) initDoKit()
    }

    private fun initDoKit() {
        DoKit.Builder(this)
            .productId("98d4d287951ea567237fa30b3f3d1a3b")
            .customKits(listOf(NetLogKit.kit))
            .build()
    }
}
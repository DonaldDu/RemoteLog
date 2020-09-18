package com.dhy.remotelog

import android.content.Context
import android.content.Intent
import com.didichuxing.doraemonkit.kit.AbstractKit

class NetLogKit : AbstractKit() {
    companion object {
        @JvmField
        val kit = NetLogKit()
    }

    override val icon: Int = R.drawable.net_log_kit
    override val name: Int = R.string.net_log_kit

    override fun onAppInit(context: Context?) {}

    override fun onClick(context: Context?) {
        if (context != null) {
            val i = Intent(context, NetLogActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(i)
        }
    }
}
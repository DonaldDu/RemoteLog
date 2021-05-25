package com.dhy.remotelog.host

import android.view.View
import com.dhy.adapterx.IViewHolder
import com.dhy.xintent.formatText
import kotlinx.android.synthetic.main.net_log_item_layout.*
import java.text.SimpleDateFormat

class NetHolder(v: View) : IViewHolder<RequestLog>(v, R.layout.net_log_item_layout) {
    companion object {
        private val dateFormat = SimpleDateFormat("HH:mm:ss")
    }

    override fun update(data: RequestLog, position: Int) {
        tvDate.text = dateFormat.format(data.date)
        tvHttp.formatText(data.method, data.httpCode)
        tvPath.formatText(data.path)
        tvParams.formatText(data.params)
        tvResponse.formatText(data.response)
    }
}
package com.dhy.remotelog

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.dhy.adapterx.AdapterX
import com.dhy.adapterx.IViewHolder
import com.dhy.remotelog.databinding.NetLogActivityBinding
import com.dhy.remotelog.databinding.NetLogItemLayoutBinding
import com.dhy.remotelog.room.RequestLog
import com.dhy.remotelog.room.getDb
import com.dhy.xintent.formatText
import com.dhy.xintent.toast
import com.yuyh.jsonviewer.library.JsonRecyclerView
import org.json.JSONObject
import java.text.SimpleDateFormat

class NetLogActivity : Activity() {
    private val binding by lazy { NetLogActivityBinding.inflate(layoutInflater) }
    private val db by lazy { getDb(this)!! }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        val pageAdapter = AdapterX(this, Holder::class, db.getLatestData())
        binding.recyclerView.adapter = pageAdapter
        pageAdapter.setOnItemClickListener {
            showDetail(it.data)
        }
        initClearHistory()
    }

    private fun initClearHistory() {
        binding.btClearHistory.setOnClickListener {
            AlertDialog.Builder(this)
                .setMessage("确定清空所有历史记录吗？")
                .setNegativeButton("关闭", null)
                .setPositiveButton("清空") { _, _ ->
                    toast("已清空所有历史数据", Toast.LENGTH_LONG)
                    db.clearAll()
                    finish()
                }.show()
        }
    }

    private fun showDetail(log: RequestLog) {
        val v = JsonRecyclerView(this)
        val json = JSONObject()
        json.put("path", log.path)
        json.put("httpCode", log.httpCode)
        json.put("cmd", log.cmd)
        json.put("server", log.server)
        if (log.headers != null) json.put("headers", JSONObject(log.headers))
        if (log.params != null) json.put("params", JSONObject(log.params))
        if (log.response != null) try {
            json.put("response", JSONObject(log.response))
        } catch (e: Exception) {
            json.put("response", log.response)
        }
        v.bindJson(json)
        AlertDialog.Builder(this)
            .setView(v)
            .setNegativeButton("关闭", null)
            .show()
    }

    class Holder(v: View) : IViewHolder<RequestLog>(v, R.layout.net_log_item_layout) {
        companion object {
            private val dateFormat = SimpleDateFormat("HH:mm:ss")
        }

        private val binding by lazy { NetLogItemLayoutBinding.bind(itemView) }
        override fun update(data: RequestLog, position: Int) {
            binding.apply {
                tvDate.text = dateFormat.format(data.date)
                tvHttp.formatText(data.method, data.httpCode)
                tvPath.formatText(data.path)
                tvParams.formatText(data.params)
                tvResponse.formatText(data.response)
            }
        }
    }
}
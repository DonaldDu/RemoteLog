package com.dhy.remotelog

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import com.dhy.adapterx.IViewHolder
import com.dhy.adapterx.PagedAdapterX
import com.dhy.remotelog.room.RequestLog
import com.dhy.remotelog.room.getDb
import com.dhy.xintent.formatText
import com.dhy.xintent.toast
import com.yuyh.jsonviewer.library.JsonRecyclerView
import kotlinx.android.synthetic.main.net_log_activity.*
import kotlinx.android.synthetic.main.net_log_item_layout.*
import org.json.JSONObject
import java.text.SimpleDateFormat

class NetLogActivity : AppCompatActivity() {
    private val db by lazy { getDb(this)!! }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.net_log_activity)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val pagedListConfig = PagedList.Config.Builder().setEnablePlaceholders(true).setPageSize(10).setInitialLoadSizeHint(20).build()

        val pagedList = LivePagedListBuilder(db.getAllByDataSource(), pagedListConfig).build()

        val pageAdapter = PagedAdapterX(this, Holder::class)
        recyclerView.adapter = pageAdapter
        pagedList.observe(this) { pageAdapter.submitList(it) }
        pageAdapter.setOnItemClickListener {
            showDetail(it.data)
        }
        initClearHistory()
    }

    private fun initClearHistory() {
        btClearHistory.setOnClickListener {
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

        override fun update(data: RequestLog, position: Int) {
            tvDate.text = dateFormat.format(data.date)
            tvHttp.formatText(data.method, data.httpCode)
            tvPath.formatText(data.path)
            tvParams.formatText(data.params)
            tvResponse.formatText(data.response)
        }
    }
}
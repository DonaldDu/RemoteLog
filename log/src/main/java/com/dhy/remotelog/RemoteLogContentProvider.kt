package com.dhy.remotelog

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import com.dhy.remotelog.RequestInfo.Companion.gson

class RemoteLogContentProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        val authority = context!!.getString(R.string.REMOTE_LOG_CONTENT_PROVIDER_AUTHORITY)
        val uri = Uri.parse("content://${authority}")
        val contentResolver = context!!.contentResolver
        val data = ContentValues()
        RemoteLogInterceptor.insert = {
            data.clear()
            data.put("data", gson.toJson(it))
            try {
                contentResolver.insert(uri, data)
            } catch (e: IllegalArgumentException) {
                installLogApp()
                e.printStackTrace()
            }
        }
        return true
    }

    private fun installLogApp() {
        context!!.startActivity(Intent(context, InstallLogAppActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = -1
    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? = null
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int = -1
}
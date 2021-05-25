package com.dhy.remotelog.host

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import com.google.gson.Gson

class LogContentProvider : ContentProvider() {
    private val db by lazy { getDb(context)!! }
    private val gson by lazy { Gson() }
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        if (values != null) {
            val json = values.getAsString("data")
            db.insert(gson.fromJson(json, RequestLog::class.java))
        }
        return null
    }

    override fun onCreate(): Boolean = true
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = -1
    override fun getType(uri: Uri): String? = null
    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? = null
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int = -1
}
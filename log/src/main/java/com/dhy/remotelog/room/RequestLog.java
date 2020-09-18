package com.dhy.remotelog.room;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.dhy.adapterx.IDiff;

@Entity(tableName = "NetLog")
public class RequestLog implements IDiff<RequestLog> {
    @PrimaryKey
    public Integer id;
    public long date;
    public String unique;
    public String method;
    public String server;
    public int httpCode;
    public String path;
    public String params;
    public String cmd;
    public String response;
    public String headers;

    @Override
    public boolean isSame(@NonNull RequestLog requestLog) {
        return id != null && id.equals(requestLog.id);
    }
}

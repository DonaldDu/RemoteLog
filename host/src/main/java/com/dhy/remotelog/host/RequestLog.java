package com.dhy.remotelog.host;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.dhy.adapterx.IDiff;

@Keep
@Entity(tableName = "NetLog")
public class RequestLog implements IDiff<RequestLog> {
    @PrimaryKey
    public Integer id;
    public long date;
    public long costTimeMS;
    public String unique;
    public String method;
    public String server;
    public int httpCode;
    public String path;
    public String params;
    public String cmd;
    public String response;
    public String headers;
    public String user;

    @Override
    public boolean isSame(@NonNull RequestLog requestLog) {
        return id != null && id.equals(requestLog.id);
    }
}

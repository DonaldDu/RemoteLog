package com.dhy.remotelog;

import androidx.annotation.Keep;

@Keep
class RequestLog {
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
}

package com.example.remotelog;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AppVersion implements Serializable, IVersion {
    @SerializedName("packagename")
    public String packageName;
    @SerializedName("versioncode")
    private int versionCode;

    @SerializedName("versiontype")
    private String type;
    @SerializedName("versionname")
    private String versionName;
    @SerializedName("forceupdate")
    private int forceUpdate;

    @Override
    public boolean isForceUpdate() {
        return forceUpdate == 1;
    }

    @Override
    public long getSize() {
        return size;
    }


    @Override
    public String getUrl() {
        return url;
    }

    public String patchUrl;


    @Override
    public String getPatchUrl() {
        return patchUrl;
    }

    public long patchSize;

    @Override
    public long getPatchSize() {
        return patchSize;
    }

    @Override
    public String getVersionName() {
        if (versionName != null) {
            return versionName;
        } else return "";
    }

    @Override
    public int getVersionCode() {
        return versionCode;
    }

    @Override
    public boolean isNew() {
        return BuildConfig.VERSION_CODE < versionCode;
    }

    @Override
    public String getLog() {
        return TextUtils.isEmpty(message) ? "" : message;
    }

    private String url;
    /**
     * 软件大小，单位 byte
     */
    @SerializedName("packagesize")
    public long size;
    private String message;
}

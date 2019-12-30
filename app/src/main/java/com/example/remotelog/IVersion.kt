package com.example.remotelog

import java.io.Serializable

interface IVersion : Serializable {
    val size: Long
    val url: String
    val patchUrl: String?
    val patchSize: Long
    val isForceUpdate: Boolean
    val versionName: String
    val versionCode: Int
    val isNew: Boolean
    /**
     * android:maxLines="15"
     */
    val log: String?
}
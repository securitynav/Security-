package com.securitynav.security.data.model

import com.google.gson.annotations.SerializedName

data class UpdateResponse(
    @SerializedName("hasUpdate") val hasUpdate: Boolean,
    @SerializedName("forceUpdate") val forceUpdate: Boolean,
    @SerializedName("versionName") val versionName: String,
    @SerializedName("downloadUrl") val downloadUrl: String,
    @SerializedName("releaseNotes") val releaseNotes: String
)

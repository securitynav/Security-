package com.securitynav.security.data.model

import com.google.gson.annotations.SerializedName

data class SecurityEventModel(
    @SerializedName("id") val id: String?,
    @SerializedName("event_type") val eventType: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("timestamp") val timestamp: Long
)

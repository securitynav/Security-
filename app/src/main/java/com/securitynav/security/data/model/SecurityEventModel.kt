package com.securitynav.security.data.model

import com.google.gson.annotations.SerializedName

data class SecurityEventModel(
    @SerializedName("event_id") val eventId: String,
    @SerializedName("event_type") val eventType: String,
    @SerializedName("description") val description: String,
    @SerializedName("severity") val severity: String,
    @SerializedName("timestamp") val timestamp: Long
)

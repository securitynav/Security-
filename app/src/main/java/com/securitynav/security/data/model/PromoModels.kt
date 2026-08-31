package com.securitynav.security.data.model

import com.google.gson.annotations.SerializedName

data class PromoRequest(
    @SerializedName("code") val code: String,
    @SerializedName("userId") val userId: String
)

data class PromoResponse(
    @SerializedName("isValid") val isValid: Boolean,
    @SerializedName("discountValue") val discountValue: Double,
    @SerializedName("message") val message: String
)

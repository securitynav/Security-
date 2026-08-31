package com.securitynav.security.data.remote

import com.securitynav.security.data.model.PromoRequest
import com.securitynav.security.data.model.PromoResponse
import com.securitynav.security.data.model.SecurityEventModel
import com.securitynav.security.data.model.TrafficPacketModel
import com.securitynav.security.data.model.UpdateResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface SecurityApiService {

    @POST("api/v1/events")
    suspend fun uploadSecurityEvent(
        @Header("Authorization") token: String,
        @Body event: SecurityEventModel
    ): Response<Void>

    @POST("api/v1/traffic")
    suspend fun uploadTrafficPacket(
        @Header("Authorization") token: String,
        @Body packet: TrafficPacketModel
    ): Response<Void>

    @GET("api/v1/vulnerabilities/trends")
    suspend fun getVulnerabilityTrends(): Response<List<com.securitynav.security.data.model.VulnerabilityTrendModel>>

    @POST("api/v1/promo/validate")
    suspend fun validatePromoCode(
        @Body request: PromoRequest
    ): Response<PromoResponse>

    @GET("api/v1/app/check-update")
    suspend fun checkUpdate(): Response<UpdateResponse>
}

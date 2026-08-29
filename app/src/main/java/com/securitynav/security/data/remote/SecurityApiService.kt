package com.securitynav.security.data.remote

import com.securitynav.security.data.model.SecurityEventModel
import com.securitynav.security.data.model.TrafficPacketModel
import retrofit2.Response
import retrofit2.http.Body
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
}

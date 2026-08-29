package com.securitynav.security.data.remote

import com.securitynav.security.data.model.SecurityEventModel
import com.securitynav.security.data.model.TrafficPacketModel
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface SecurityApiService {

    @POST("api/v1/telemetry/traffic")
    suspend fun uploadTrafficPacket(
        @Header("Authorization") authToken: String,
        @Body packet: TrafficPacketModel
    ): Response<Void>

    @POST("api/v1/security/events")
    suspend fun reportSecurityEvent(
        @Header("Authorization") authToken: String,
        @Body event: SecurityEventModel
    ): Response<Void>
}

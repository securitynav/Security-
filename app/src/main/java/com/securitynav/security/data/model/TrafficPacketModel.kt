package com.securitynav.security.data.model

import com.google.gson.annotations.SerializedName

data class TrafficPacketModel(
    @SerializedName("packet_id") val packetId: String?,
    @SerializedName("source_ip") val sourceIp: String?,
    @SerializedName("destination_ip") val destinationIp: String?,
    @SerializedName("source_port") val sourcePort: Int,
    @SerializedName("destination_port") val destinationPort: Int,
    @SerializedName("protocol") val protocol: String?,
    @SerializedName("payload_size") val payloadSize: Int,
    @SerializedName("timestamp") val timestamp: Long
)

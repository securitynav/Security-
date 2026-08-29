package com.securitynav.security.data.model

import com.google.gson.annotations.SerializedName

data class TrafficPacketModel(
    @SerializedName("packet_id") val packetId: String,
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("source_ip") val sourceIp: String,
    @SerializedName("destination_ip") val destinationIp: String,
    @SerializedName("method") val method: String,
    @SerializedName("payload_size") val payloadSize: Long,
    @SerializedName("bandwidth_usage") val bandwidthUsage: Double,
    @SerializedName("status_code") val statusCode: Int
)

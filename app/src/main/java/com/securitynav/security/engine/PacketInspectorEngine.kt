package com.securitynav.security.engine

import android.content.Context
import com.securitynav.security.data.database.SecurityDatabase
import java.nio.ByteBuffer

data class PacketMetric(
    val timestamp: Long = System.currentTimeMillis(),
    val sourceIp: String,
    val destinationIp: String,
    val sourcePort: Int,
    val destinationPort: Int,
    val protocol: String,
    val httpMethod: String,
    val urlOrHost: String,
    val payloadSize: Int,
    val isSuspicious: Boolean
)

class PacketInspectorEngine(private val context: Context) {

    private val dbHelper = SecurityDatabase(context)

    fun inspectPacketBuffer(buffer: ByteBuffer, length: Int): PacketMetric? {
        if (length < 20) return null // Cabecera IP incompleta

        val ipVersion = (buffer.get(0).toInt() shr 4) and 0x0F
        if (ipVersion != 4) return null // Enfocado en IPv4

        val srcIp = "${buffer.get(12).toInt() and 0xFF}.${buffer.get(13).toInt() and 0xFF}.${buffer.get(14).toInt() and 0xFF}.${buffer.get(15).toInt() and 0xFF}"
        val dstIp = "${buffer.get(16).toInt() and 0xFF}.${buffer.get(17).toInt() and 0xFF}.${buffer.get(18).toInt() and 0xFF}.${buffer.get(19).toInt() and 0xFF}"

        val protocolType = buffer.get(9).toInt() and 0xFF
        val protocolStr = when (protocolType) {
            6 -> "TCP"
            17 -> "UDP"
            1 -> "ICMP"
            else -> "OTHER ($protocolType)"
        }

        var srcPort = 0
        var dstPort = 0
        var httpMethod = "RAW"
        var urlOrHost = "N/A"
        var isSuspicious = false

        if (protocolType == 6 && length > 40) { // TCP payload
            srcPort = ((buffer.get(20).toInt() and 0xFF) shl 8) or (buffer.get(21).toInt() and 0xFF)
            dstPort = ((buffer.get(22).toInt() and 0xFF) shl 8) or (buffer.get(23).toInt() and 0xFF)

            val payloadOffset = 40
            if (length > payloadOffset) {
                val payloadBytes = ByteArray(length - payloadOffset)
                buffer.position(payloadOffset)
                buffer.get(payloadBytes, 0, payloadBytes.size)
                val payloadText = String(payloadBytes, Charsets.US_ASCII)

                when {
                    payloadText.startsWith("GET ") -> {
                        httpMethod = "GET"
                        urlOrHost = extractHttpHostOrPath(payloadText)
                    }
                    payloadText.startsWith("POST ") -> {
                        httpMethod = "POST"
                        urlOrHost = extractHttpHostOrPath(payloadText)
                        isSuspicious = payloadText.contains("password") || payloadText.contains("token")
                    }
                    payloadText.startsWith("PUT ") -> httpMethod = "PUT"
                    payloadText.startsWith("DELETE ") -> httpMethod = "DELETE"
                    payloadText.startsWith("CONNECT ") -> httpMethod = "HTTPS_TUNNEL"
                }
            }
        }

        val metric = PacketMetric(
            sourceIp = srcIp,
            destinationIp = dstIp,
            sourcePort = srcPort,
            destinationPort = dstPort,
            protocol = protocolStr,
            httpMethod = httpMethod,
            urlOrHost = urlOrHost,
            payloadSize = length,
            isSuspicious = isSuspicious
        )

        if (httpMethod != "RAW" || isSuspicious) {
            dbHelper.logEvent(
                "TRAFFIC_INSPECTION",
                "[$protocolStr][$httpMethod] $srcIp:$srcPort -> $dstIp:$dstPort | Payload: ${length}B | Details: $urlOrHost"
            )
        }

        return metric
    }

    private fun extractHttpHostOrPath(payloadText: String): String {
        val lines = payloadText.split("\r\n")
        val requestLine = lines.firstOrNull() ?: ""
        val hostLine = lines.firstOrNull { it.startsWith("Host:", ignoreCase = true) } ?: ""
        return "$hostLine | Path: ${requestLine.take(40)}"
    }
}

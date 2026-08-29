package com.securitynav.security.engine

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

data class IPLocationData(
    val ip: String,
    val country: String,
    val city: String,
    val isp: String,
    val isHostingOrProxy: Boolean,
    val threatScore: Int
)

class IPThreatResolver {

    fun resolveIP(ip: String, callback: (IPLocationData) -> Unit) {
        if (ip.startsWith("127.") || ip.startsWith("10.") || ip.startsWith("192.168.")) {
            callback(IPLocationData(ip, "Local", "Local Network", "Private Network", false, 0))
            return
        }

        thread {
            try {
                val url = URL("http://ip-api.com/json/$ip?fields=status,message,country,city,isp,proxy,hosting")
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 3000
                connection.readTimeout = 3000

                if (connection.responseCode == 200) {
                    val stream = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(stream)

                    if (json.optString("status") == "success") {
                        val country = json.optString("country", "Desconocido")
                        val city = json.optString("city", "Desconocida")
                        val isp = json.optString("isp", "Desconocido")
                        val isProxy = json.optBoolean("proxy", false)
                        val isHosting = json.optBoolean("hosting", false)
                        val isThreat = isProxy || isHosting
                        val score = if (isThreat) 85 else 10

                        callback(
                            IPLocationData(
                                ip = ip,
                                country = country,
                                city = city,
                                isp = isp,
                                isHostingOrProxy = isThreat,
                                threatScore = score
                            )
                        )
                        return@thread
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            callback(IPLocationData(ip, "Desconocido", "Desconocida", "Desconocido", false, 0))
        }
    }
}

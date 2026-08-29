package com.securitynav.security.engine

import java.util.concurrent.ConcurrentHashMap

data class BlockRule(
    val target: String,
    val reason: String,
    val timestamp: Long
)

class ThreatBlockerEngine {

    private val blacklistedIPs = ConcurrentHashMap<String, BlockRule>()
    private val blacklistedDomains = ConcurrentHashMap<String, BlockRule>()

    init {
        // Carga inicial de firmas de prueba y C2s conocidos
        addIPToBlacklist("185.220.101.5", "Nodo de salida Tor / C2 conocido")
        addIPToBlacklist("193.142.146.35", "Servidor de rastreo y exfiltración")
        addDomainToBlacklist("phishing-fake-bank.com", "Sitio de Phishing detectado")
        addDomainToBlacklist("malware-distribution-node.ru", "Servidor de descarga de Malware")
    }

    fun addIPToBlacklist(ip: String, reason: String) {
        blacklistedIPs[ip] = BlockRule(ip, reason, System.currentTimeMillis())
    }

    fun addDomainToBlacklist(domain: String, reason: String) {
        blacklistedDomains[domain] = BlockRule(domain, reason, System.currentTimeMillis())
    }

    fun isIPBlocked(ip: String): Boolean {
        return blacklistedIPs.containsKey(ip)
    }

    fun isDomainBlocked(domain: String): Boolean {
        return blacklistedDomains.keys.any { domain.contains(it) }
    }

    fun getBlockReason(ipOrDomain: String): String {
        return blacklistedIPs[ipOrDomain]?.reason 
            ?: blacklistedDomains[ipOrDomain]?.reason 
            ?: "Amenaza bloqueada por política de seguridad activa"
    }

    fun executeActiveCountermeasure(ip: String, details: String): String {
        // Bloqueo instantáneo de la IP en la capa TUN
        addIPToBlacklist(ip, "Contra-medida activa gatillada: $details")
        return "ACTUACIÓN DEFENSIVA: IP $ip neutralizada y añadida a la lista negra."
    }
}

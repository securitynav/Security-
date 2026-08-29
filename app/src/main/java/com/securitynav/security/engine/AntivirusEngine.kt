package com.securitynav.security.engine

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

data class ThreatAnalysisResult(
    val packageName: String,
    val appName: String,
    val riskScore: Int, // 0 a 100
    val riskLevel: String, // CLEAN, SUSPICIOUS, CRITICAL
    val detectedRisks: List<String>
)

class AntivirusEngine(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager

    fun scanInstalledApplications(): List<ThreatAnalysisResult> {
        val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        val results = mutableListOf<ThreatAnalysisResult>()

        for (app in installedApps) {
            // Filtrar apps del sistema esenciales
            if ((app.flags and ApplicationInfo.FLAG_SYSTEM) != 0) continue

            val result = analyzeAppPermissions(app)
            if (result.riskScore > 20) {
                results.add(result)
            }
        }
        return results.sortedByDescending { it.riskScore }
    }

    private fun analyzeAppPermissions(appInfo: ApplicationInfo): ThreatAnalysisResult {
        val risks = mutableListOf<String>()
        var score = 0

        try {
            val pkgInfo = packageManager.getPackageInfo(appInfo.packageName, PackageManager.GET_PERMISSIONS)
            val permissions = pkgInfo.requestedPermissions ?: arrayOf()

            val hasAccessibility = permissions.contains("android.permission.BIND_ACCESSIBILITY_SERVICE")
            val hasSystemAlert = permissions.contains("android.permission.SYSTEM_ALERT_WINDOW")
            val hasInternet = permissions.contains("android.permission.INTERNET")
            val hasSms = permissions.contains("android.permission.RECEIVE_SMS") || permissions.contains("android.permission.READ_SMS")
            val hasLocation = permissions.contains("android.permission.ACCESS_FINE_LOCATION")

            if (hasAccessibility && hasInternet) {
                score += 50
                risks.add("Peligro de Keylogging: Permiso de Accesibilidad + Acceso a Internet")
            }

            if (hasSystemAlert && hasInternet) {
                score += 30
                risks.add("Peligro de Superposición (Overlay Attack): Dibujar sobre otras apps + Internet")
            }

            if (hasSms && hasInternet) {
                score += 35
                risks.add("Peligro de Intercepción 2FA: Lectura de SMS + Internet")
            }

            if (hasLocation && hasInternet && (appInfo.packageName.contains("calculator") || appInfo.packageName.contains("flashlight"))) {
                score += 40
                risks.add("Anomalía Funcional: Aplicación básica solicitando Ubicación e Internet")
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        val riskLevel = when {
            score >= 60 -> "CRITICAL"
            score >= 30 -> "SUSPICIOUS"
            else -> "CLEAN"
        }

        val appName = packageManager.getApplicationLabel(appInfo).toString()
        return ThreatAnalysisResult(appInfo.packageName, appName, score, riskLevel, risks)
    }

    fun isURLSafe(url: String, blocker: ThreatBlockerEngine): Boolean {
        val cleanUrl = url.lowercase().replace("http://", "").replace("https://", "").split("/")[0]
        return !blocker.isDomainBlocked(cleanUrl)
    }
}

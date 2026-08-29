package com.securitynav.security.util

import android.content.Context
import android.content.pm.PackageManager
import android.net.TrafficStats

data class AppTrafficData(
    val uid: Int,
    val appName: String,
    val packageName: String,
    val rxBytes: Long,
    val txBytes: Long,
    val totalBytes: Long
)

class AppTrafficMonitor(private val context: Context) {

    fun getTrafficForUid(uid: Int): AppTrafficData {
        val rx = TrafficStats.getUidRxBytes(uid)
        val tx = TrafficStats.getUidTxBytes(uid)

        val rxBytes = if (rx != TrafficStats.UNSUPPORTED.toLong()) rx else 0L
        val txBytes = if (tx != TrafficStats.UNSUPPORTED.toLong()) tx else 0L

        val pm = context.packageManager
        val packages = pm.getPackagesForUid(uid)
        val pkgName = packages?.firstOrNull() ?: "unknown"
        val appName = try {
            val appInfo = pm.getApplicationInfo(pkgName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            pkgName
        }

        return AppTrafficData(
            uid = uid,
            appName = appName,
            packageName = pkgName,
            rxBytes = rxBytes,
            txBytes = txBytes,
            totalBytes = rxBytes + txBytes
        )
    }

    fun getAllAppsTraffic(): List<AppTrafficData> {
        val pm = context.packageManager
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val trafficList = mutableListOf<AppTrafficData>()

        for (app in installedApps) {
            val data = getTrafficForUid(app.uid)
            if (data.totalBytes > 0) {
                trafficList.add(data)
            }
        }

        return trafficList.sortedByDescending { it.totalBytes }
    }

    fun getTotalRxBytes(): Long {
        val rx = TrafficStats.getTotalRxBytes()
        return if (rx != TrafficStats.UNSUPPORTED.toLong()) rx else 0L
    }

    fun getTotalTxBytes(): Long {
        val tx = TrafficStats.getTotalTxBytes()
        return if (tx != TrafficStats.UNSUPPORTED.toLong()) tx else 0L
    }
}

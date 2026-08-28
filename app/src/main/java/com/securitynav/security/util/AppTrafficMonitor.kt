package com.securitynav.security.util

import android.content.Context
import android.content.pm.PackageManager
import android.net.TrafficStats
import android.util.Log

data class AppTrafficInfo(
    val packageName: String,
    val appName: String,
    val rxBytes: Long,
    val txBytes: Long
)

object AppTrafficMonitor {
    private const val TAG = "AppTrafficMonitor"

    fun getInstalledAppsTraffic(context: Context): List<AppTrafficInfo> {
        val pm = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val trafficList = mutableListOf<AppTrafficInfo>()

        for (appInfo in packages) {
            val uid = appInfo.uid
            val rx = TrafficStats.getUidRxBytes(uid)
            val tx = TrafficStats.getUidTxBytes(uid)

            if (rx != TrafficStats.UNSUPPORTED && tx != TrafficStats.UNSUPPORTED && (rx > 0 || tx > 0)) {
                val appName = pm.getApplicationLabel(appInfo).toString()
                trafficList.add(AppTrafficInfo(appInfo.packageName, appName, rx, tx))
                Log.d(TAG, "App: $appName [${appInfo.packageName}] - RX: $rx bytes, TX: $tx bytes")
            }
        }
        return trafficList
    }
}

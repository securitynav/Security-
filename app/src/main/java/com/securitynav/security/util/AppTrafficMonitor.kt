package com.securitynav.security.util

import android.net.TrafficStats

data class AppTrafficData(
    val uid: Int,
    val rxBytes: Long,
    val txBytes: Long
)

class AppTrafficMonitor {

    fun getTrafficForUid(uid: Int): AppTrafficData {
        val rx = TrafficStats.getUidRxBytes(uid)
        val tx = TrafficStats.getUidTxBytes(uid)

        val rxBytes = if (rx != -1L) rx else 0L
        val txBytes = if (tx != -1L) tx else 0L

        return AppTrafficData(uid, rxBytes, txBytes)
    }

    fun getTotalRxBytes(): Long {
        val rx = TrafficStats.getTotalRxBytes()
        return if (rx != -1L) rx else 0L
    }

    fun getTotalTxBytes(): Long {
        val tx = TrafficStats.getTotalTxBytes()
        return if (tx != -1L) tx else 0L
    }
}

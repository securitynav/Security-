package com.securitynav.security.engine

import android.content.Context
import android.telephony.TelephonyManager

class CellAnomalyDetector(private val context: Context) {
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    @android.annotation.SuppressLint("MissingPermission")
    fun isDangerousDowngrade(): Boolean {
        val networkType = telephonyManager.networkType
        // Detectar si la red cambia abruptamente a tecnologías sin cifrado fuerte (GSM/2G/GPRS/EDGE)
        return when (networkType) {
            TelephonyManager.NETWORK_TYPE_GPRS,
            TelephonyManager.NETWORK_TYPE_EDGE,
            TelephonyManager.NETWORK_TYPE_GSM -> true
            else -> false
        }
    }
}

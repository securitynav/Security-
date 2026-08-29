package com.securitynav.security.services

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import com.securitynav.security.data.database.SecurityDatabase

class TrafficMonitorService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private lateinit var dbHelper: SecurityDatabase

    override fun onCreate() {
        super.onCreate()
        dbHelper = SecurityDatabase(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (vpnInterface == null) {
            setupVpn()
        }
        return START_STICKY
    }

    private fun setupVpn() {
        try {
            val builder = Builder()
                .addAddress("10.0.0.2", 24)
                .addRoute("0.0.0.0", 0)
                .setSession("SecurityNavVPN")

            vpnInterface = builder.establish()
            dbHelper.logEvent("VPN_TRAFFIC_MONITOR", "Escudo VPN de filtrado de tráfico establecido.")
        } catch (e: Exception) {
            dbHelper.logEvent("VPN_TRAFFIC_ERROR", "Error al establecer interfaz VPN: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        vpnInterface?.close()
        vpnInterface = null
    }
}

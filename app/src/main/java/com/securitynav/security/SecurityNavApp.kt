package com.securitynav.security

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class SecurityNavApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createVpnNotificationChannel()
    }

    private fun createVpnNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "vpn_service_channel",
                "SecurityNav VPN Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Canal para notificaciones del servicio VPN en primer plano"
            }
            (getSystemService(NotificationManager::class.java))?.createNotificationChannel(channel)
        }
    }
}

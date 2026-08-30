package com.securitynav.security

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build

class SecurityNavApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createVpnNotificationChannel()
    }

    private fun createVpnNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "vpn_service_channel"
            val channelName = "SecurityNav VPN Service"
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT // default to allow subtle sounds
            ).apply {
                description = "Canal para notificaciones del servicio VPN en primer plano"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 100, 50)
                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                setSound(soundUri, audioAttributes)
            }
            (getSystemService(NotificationManager::class.java))?.createNotificationChannel(channel)
        }
    }
}

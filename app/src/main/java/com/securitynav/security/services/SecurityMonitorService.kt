package com.securitynav.security.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.securitynav.security.data.database.SecurityDatabase
import com.securitynav.security.notifications.NotificationHelper

class SecurityMonitorService : Service() {

    private lateinit var dbHelper: SecurityDatabase
    private val NOTIFICATION_ID = 101

    override fun onCreate() {
        super.onCreate()
        dbHelper = SecurityDatabase(this)
        startForeground(NOTIFICATION_ID, createForegroundNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        dbHelper.logEvent("SECURITY_MONITOR", "Orquestador de seguridad activo en primer plano.")
        return START_STICKY
    }

    private fun createForegroundNotification(): Notification {
        val channelId = NotificationHelper.CHANNEL_SECURITY_STATUS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Escudo Activo",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("SecurityNav Protegiendo")
            .setContentText("El escudo en tiempo real está activo.")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

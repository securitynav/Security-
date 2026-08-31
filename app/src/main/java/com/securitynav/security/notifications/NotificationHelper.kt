package com.securitynav.security.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.securitynav.security.ui.MainActivity

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_CRITICAL_ALERTS = "channel_critical_alerts"
        const val CHANNEL_WARNINGS = "channel_warnings"
        const val CHANNEL_SECURITY_STATUS = "channel_security_status"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            // 1. Canal de Alertas Críticas (Sonido de alarma + Vibración intensa + Heads-up)
            val criticalChannel = NotificationChannel(
                CHANNEL_CRITICAL_ALERTS,
                "Alertas Críticas de Seguridad",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones prioritarias sobre amenazas e intrusiones detectadas"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 1000)
                setSound(alarmSound, audioAttributes)
            }

            // 2. Canal de Advertencias y Eventos Secundarios
            val notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val warningChannel = NotificationChannel(
                CHANNEL_WARNINGS,
                "Advertencias del Sistema",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones de análisis e información preventiva"
                enableVibration(true)
                setSound(notificationSound, null)
            }

            // 3. Canal Silencioso para Servicio de Monitoreo
            val statusChannel = NotificationChannel(
                CHANNEL_SECURITY_STATUS,
                "Estado del Escudo de Seguridad",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Indicador de protección en tiempo real"
                setShowBadge(false)
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(criticalChannel)
            manager.createNotificationChannel(warningChannel)
            manager.createNotificationChannel(statusChannel)
        }
    }

    fun triggerCriticalAlert(title: String, details: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        val builder = NotificationCompat.Builder(context, CHANNEL_CRITICAL_ALERTS)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle("🚨 AMENAZA CRÍTICA: $title")
            .setContentText(details)
            .setStyle(NotificationCompat.BigTextStyle().bigText(details))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setSound(alarmSound)
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 1000))
            .setContentIntent(pendingIntent)

        val notificationManager = NotificationManagerCompat.from(context)
        try {
            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun triggerWarningAlert(title: String, details: String) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_WARNINGS)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("⚠️ Advertencia: $title")
            .setContentText(details)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = NotificationManagerCompat.from(context)
        try {
            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}

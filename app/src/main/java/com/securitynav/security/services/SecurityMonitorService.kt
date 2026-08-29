package com.securitynav.security.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.securitynav.security.data.database.SecurityDatabase
import com.securitynav.security.engine.CellAnomalyDetector
import com.securitynav.security.forensics.ForensicCollector
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class SecurityMonitorService : Service() {

    private lateinit var database: SecurityDatabase
    private lateinit var cellDetector: CellAnomalyDetector
    private lateinit var forensicCollector: ForensicCollector
    private var scheduler: ScheduledExecutorService? = null

    override fun onCreate() {
        super.onCreate()
        database = SecurityDatabase(this)
        cellDetector = CellAnomalyDetector(this)
        forensicCollector = ForensicCollector(this)
        
        startForegroundService()
        startSecurityAuditLoop()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startForegroundService() {
        val channelId = "security_monitor_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Monitor de Seguridad Activo",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("SecurityNav Protegiendo")
            .setContentText("Supervisando anomalías de red y accesibilidad...")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1001, notification)
    }

    private fun startSecurityAuditLoop() {
        scheduler = Executors.newSingleThreadScheduledExecutor()
        scheduler?.scheduleWithFixedDelay({
            try {
                if (cellDetector.isDangerousDowngrade()) {
                    val db = database.getWritableEncryptedDatabase("master_key_temp")
                    db.execSQL(
                        "INSERT INTO security_logs (timestamp, event_type, details) VALUES (?, ?, ?)",
                        arrayOf(System.currentTimeMillis(), "CELL_DOWNGRADE", "Downgrade de red a 2G/GSM detectado")
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, 0, 15, TimeUnit.SECONDS)
    }

    override fun onDestroy() {
        super.onDestroy()
        scheduler?.shutdownNow()
    }
}

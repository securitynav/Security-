package com.securitynav.security.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class TrafficMonitorService : VpnService(), Runnable {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var vpnThread: Thread? = null
    @Volatile private var isRunning = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopVpn()
            return START_NOT_STICKY
        }

        startForegroundService()
        setupVpn()
        return START_STICKY
    }

    private fun startForegroundService() {
        val channelId = "traffic_monitor_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Monitoreo de Tráfico VPN",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("SecurityNav Traffic Firewall")
            .setContentText("Supervisando paquetes de red en tiempo real...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(2002, notification)
    }

    private fun setupVpn() {
        if (isRunning) return

        try {
            val builder = Builder()
                .setSession("SecurityNavVpn")
                .addAddress("10.1.10.1", 24)
                .addRoute("0.0.0.0", 0)
                .setMtu(1500)

            vpnInterface = builder.establish()
            isRunning = true

            vpnThread = Thread(this, "SecurityNav-VPN-Thread").apply { start() }
        } catch (e: Exception) {
            e.printStackTrace()
            stopVpn()
        }
    }

    override fun run() {
        val pfd = vpnInterface ?: return
        val input = FileInputStream(pfd.fileDescriptor)
        val output = FileOutputStream(pfd.fileDescriptor)
        val buffer = ByteArray(32767)

        try {
            while (isRunning) {
                val length = input.read(buffer)
                if (length > 0) {
                    // Procesamiento e inspección de encabezados de red IP/TCP/UDP
                    output.write(buffer, 0, length)
                }
            }
        } catch (e: IOException) {
            // Cierre normal del socket o interrupción de interfaz
        } finally {
            try {
                input.close()
                output.close()
            } catch (ignored: Exception) {}
        }
    }

    private fun stopVpn() {
        isRunning = false
        vpnThread?.interrupt()
        try {
            vpnInterface?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        vpnInterface = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }

    companion object {
        const val ACTION_STOP = "com.securitynav.security.ACTION_STOP_VPN"
    }
}

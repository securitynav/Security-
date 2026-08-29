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
import com.securitynav.security.data.database.SecurityDatabase
import com.securitynav.security.engine.IPThreatResolver
import com.securitynav.security.engine.ThreatBlockerEngine
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class TrafficMonitorService : VpnService(), Runnable {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var vpnThread: Thread? = null
    @Volatile private var isRunning = false

    private lateinit var threatBlocker: ThreatBlockerEngine
    private lateinit var ipResolver: IPThreatResolver
    private lateinit var database: SecurityDatabase

    override fun onCreate() {
        super.onCreate()
        threatBlocker = ThreatBlockerEngine()
        ipResolver = IPThreatResolver()
        database = SecurityDatabase(this)
    }

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
                "Escudo de Tráfico y Antivirus ActiveShield",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("SecurityNav ActiveShield")
            .setContentText("Filtrando tráfico, sitios maliciosos y bloqueando intrusos...")
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
                    val destIp = extractDestinationIP(buffer, length)
                    
                    if (destIp != null && threatBlocker.isIPBlocked(destIp)) {
                        // CONTRA-MEDIDA: Descartar paquete (Drop Packet)
                        ipResolver.resolveIP(destIp) { info ->
                            logSecurityEvent(
                                "AMENAZA_BLOQUEADA",
                                "Paquete bloqueado hacia IP: $destIp (${info.country}, ${info.city} - ISP: ${info.isp})"
                            )
                        }
                        continue // Saltar reescritura = Bloqueo total
                    }

                    // Si es una IP nueva, resolver ubicación en segundo plano
                    if (destIp != null && !destIp.startsWith("10.") && !destIp.startsWith("192.168.")) {
                        ipResolver.resolveIP(destIp) { info ->
                            if (info.isHostingOrProxy) {
                                threatBlocker.executeActiveCountermeasure(destIp, "IP Hostil / Proxy / Tor detectada")
                            }
                        }
                    }

                    output.write(buffer, 0, length)
                }
            }
        } catch (e: IOException) {
            // Cierre normal de interfaz
        } finally {
            try {
                input.close()
                output.close()
            } catch (ignored: Exception) {}
        }
    }

    private fun extractDestinationIP(buffer: ByteArray, length: Int): String? {
        if (length < 20) return null
        val version = (buffer[0].toInt() and 0xF0) shr 4
        if (version == 4) { // IPv4
            val ip = "${buffer[16].toInt() and 0xFF}.${buffer[17].toInt() and 0xFF}.${buffer[18].toInt() and 0xFF}.${buffer[19].toInt() and 0xFF}"
            return ip
        }
        return null
    }

    private fun logSecurityEvent(type: String, details: String) {
        try {
            val db = database.getWritableEncryptedDatabase("master_key_temp")
            db.execSQL(
                "INSERT INTO security_logs (timestamp, event_type, details) VALUES (?, ?, ?)",
                arrayOf(System.currentTimeMillis(), type, details)
            )
        } catch (e: Exception) {
            e.printStackTrace()
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

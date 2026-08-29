package com.securitynav.security.services

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import com.securitynav.security.data.database.SecurityDatabase
import com.securitynav.security.notifications.SecurityNotificationManager
import java.io.FileInputStream
import java.nio.ByteBuffer

class TrafficMonitorService : VpnService(), Runnable {

    private var vpnThread: Thread? = null
    private var vpnInterface: ParcelFileDescriptor? = null
    @Volatile private var isRunning = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isRunning) {
            isRunning = true
            vpnThread = Thread(this, "TrafficVPNThread").apply { start() }
        }
        return START_STICKY
    }

    override fun run() {
        try {
            val builder = Builder()
                .addAddress("10.0.0.2", 32)
                .addRoute("0.0.0.0", 0)
                .setSession("SecurityNavVPN")

            vpnInterface = builder.establish()
            val inputStream = FileInputStream(vpnInterface?.fileDescriptor)
            val buffer = ByteBuffer.allocate(32768)

            val dbHelper = SecurityDatabase(this)
            val notificationManager = SecurityNotificationManager(this)

            while (isRunning) {
                val readBytes = inputStream.read(buffer.array())
                // FIX: Validar la lectura real de bytes para evitar que cuente infinitamente
                if (readBytes > 0) {
                    val packetData = String(buffer.array(), 0, readBytes, Charsets.ISO_8859_1)
                    var method = "OTHER"
                    if (packetData.contains("GET ")) method = "GET"
                    else if (packetData.contains("POST ")) method = "POST"

                    val logDetail = "IP Packet Processed: $readBytes B | Verb: [$method]"
                    val db = dbHelper.getWritableEncryptedDatabase()
                    db.execSQL(
                        "INSERT INTO security_logs (timestamp, event_type, details) VALUES (?, ?, ?)",
                        arrayOf(System.currentTimeMillis(), "VPN_TRAFFIC", logDetail)
                    )
                    db.close()

                    if (method == "POST") {
                        notificationManager.sendSecurityAlert(
                            "Inspección de Tráfico Outbound",
                            "Se detectó envío de paquetes POST ($readBytes Bytes)."
                        )
                    }

                    buffer.clear()
                } else {
                    // Prevenir el bucle activo consumiendo CPU si no hay datos
                    Thread.sleep(100)
                }
            }
        } catch (e: Exception) {
            Log.e("TrafficVPN", "Error en túnel de inspección", e)
        } finally {
            closeVpn()
        }
    }

    private fun closeVpn() {
        try {
            vpnInterface?.close()
            vpnInterface = null
        } catch (e: Exception) {
            Log.e("TrafficVPN", "Error cerrando interfaz", e)
        }
    }

    override fun onDestroy() {
        isRunning = false
        closeVpn()
        super.onDestroy()
    }
}

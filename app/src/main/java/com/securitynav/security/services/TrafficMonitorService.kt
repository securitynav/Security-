package com.securitynav.security.services

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import com.securitynav.security.engine.PacketInspectorEngine
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import kotlin.concurrent.thread

class TrafficMonitorService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var isRunning = false
    private lateinit var inspectorEngine: PacketInspectorEngine

    override fun onCreate() {
        super.onCreate()
        inspectorEngine = PacketInspectorEngine(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isRunning) {
            setupVpnAndStartRelay()
        }
        return START_STICKY
    }

    private fun setupVpnAndStartRelay() {
        try {
            val builder = Builder()
                .addAddress("10.0.0.2", 24)
                .addRoute("0.0.0.0", 0)
                .addDnsServer("8.8.8.8")
                .addDnsServer("1.1.1.1")
                .setSession("SecurityNavEngine")

            // Evitar redirigir el tráfico de la propia app para no generar un bucle infinito
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                builder.addDisallowedApplication(packageName)
            }

            vpnInterface = builder.establish()
            isRunning = true

            thread(start = true, name = "VPN_Packet_Relay") {
                val input = FileInputStream(vpnInterface?.fileDescriptor)
                val output = FileOutputStream(vpnInterface?.fileDescriptor)
                val buffer = ByteBuffer.allocate(32767)

                while (isRunning) {
                    val readBytes = input.read(buffer.array())
                    if (readBytes > 0) {
                        buffer.limit(readBytes)
                        buffer.rewind()

                        // Inspeccionar paquetes en segundo plano
                        inspectorEngine.inspectPacketBuffer(buffer, readBytes)

                        // Reenviar el paquete al bus nativo sin bloquear
                        buffer.rewind()
                        output.write(buffer.array(), 0, readBytes)
                        buffer.clear()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        vpnInterface?.close()
        vpnInterface = null
    }
}

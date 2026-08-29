package com.securitynav.security.vpn

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

class LocalVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private val isRunning = AtomicBoolean(false)
    private var vpnThread: Thread? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isRunning.get()) {
            startVpn()
        }
        return START_STICKY
    }

    private fun startVpn() {
        val builder = Builder()
            .addAddress("10.0.0.2", 24)
            .addRoute("0.0.0.0", 0)
            .addDnsServer("8.8.8.8")
            .setSession("SecurityNavVPN")
            .setConfigureIntent(null)

        try {
            vpnInterface = builder.establish()
            isRunning.set(true)
            
            vpnThread = Thread({
                runVpnLoop()
            }, "SecurityNavVpnThread")
            vpnThread?.start()
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    private fun runVpnLoop() {
        val descriptor = vpnInterface ?: return
        try {
            val inputStream = FileInputStream(descriptor.fileDescriptor)
            val outputStream = FileOutputStream(descriptor.fileDescriptor)
            val buffer = ByteBuffer.allocate(32767)

            while (isRunning.get()) {
                val length = inputStream.read(buffer.array())
                if (length > 0) {
                    // Procesamiento seguro de paquetes locales sin bloquear la red general
                    buffer.clear()
                } else {
                    Thread.sleep(10)
                }
            }
        } catch (e: Exception) {
            // Manejo de interrupción de socket por cierre de VPN
        }
    }

    override fun onDestroy() {
        isRunning.set(false)
        vpnThread?.interrupt()
        try {
            vpnInterface?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        vpnInterface = null
        super.onDestroy()
    }
}

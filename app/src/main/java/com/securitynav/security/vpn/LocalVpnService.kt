package com.securitynav.security.vpn

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import com.securitynav.security.ui.MainActivity
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer

class LocalVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var isRunning = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isRunning = true
        startVpn()
        return START_STICKY
    }

    private fun startVpn() {
        try {
            val builder = Builder()
                .addAddress("10.0.0.2", 24)
                .addRoute("0.0.0.0", 0)
                .setSession("SecurityNavVPN")
            
            vpnInterface = builder.establish()
            
            // Notificar o iniciar ciclo de lectura de paquetes de red de forma segura
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            // Mantiene el servicio enlazado y listo
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        vpnInterface?.close()
    }
}

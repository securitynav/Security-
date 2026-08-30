package com.securitynav.security.vpn

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.securitynav.security.R
import com.securitynav.security.monitor.NetworkMonitor
import kotlinx.coroutines.*
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * LocalVpnService - a minimal, coroutine-powered VPN service template.
 * It establishes a VPN interface and reads/writes packets on a background coroutine.
 * Note: Production-grade packet parsing/filtering requires careful implementation.
 */
class LocalVpnService : VpnService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var vpnInterface: ParcelFileDescriptor? = null
    private var workerJob: Job? = null

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundIfNeeded()

        // Build a very small VPN interface. Adapt addresses/routes as needed.
        val builder = Builder()
            .setSession("SecurityNavVPN")
            .addAddress("10.0.0.2", 32)
            .addRoute("0.0.0.0", 0)
            .setMtu(1500)

        vpnInterface?.close()
        vpnInterface = builder.establish()

        vpnInterface?.let { pfd ->
            workerJob?.cancel()
            workerJob = serviceScope.launch {
                runVpnLoop(pfd)
            }
        }

        return Service.START_STICKY
    }

    private suspend fun runVpnLoop(pfd: ParcelFileDescriptor) {
        // Read packets from VPN interface and count bytes for monitoring
        val `in` = FileInputStream(pfd.fileDescriptor)
        val out = FileOutputStream(pfd.fileDescriptor)
        val buffer = ByteArray(32767)

        try {
            while (isActive) {
                val read = withContext(Dispatchers.IO) { `in`.read(buffer) }
                if (read > 0) {
                    NetworkMonitor.recordInbound(read.toLong())
                    // For template purposes we echo the packet back (unsafe for production)
                    withContext(Dispatchers.IO) { out.write(buffer, 0, read) }
                    NetworkMonitor.recordOutbound(read.toLong())
                } else {
                    delay(50)
                }
            }
        } catch (e: Exception) {
            // Log or handle errors appropriately (avoid exposing sensitive data in logs)
            e.printStackTrace()
        } finally {
            try { `in`.close() } catch (_: Exception) {}
            try { out.close() } catch (_: Exception) {}
        }
    }

    private fun startForegroundIfNeeded() {
        val notification = createNotification()
        startForeground(1001, notification)
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, Class.forName("com.securitynav.security.ui.MainActivity"))
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, "securitynav_vpn_channel")
            .setContentTitle("SecurityNav VPN")
            .setContentText("VPN is running")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        workerJob?.cancel()
        serviceScope.cancel()
        vpnInterface?.close()
        vpnInterface = null
    }

    override fun onRevoke() {
        super.onRevoke()
        onDestroy()
    }
}

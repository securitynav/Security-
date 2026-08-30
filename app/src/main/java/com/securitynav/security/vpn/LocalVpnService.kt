package com.securitynav.security.vpn

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.securitynav.security.R
import com.securitynav.security.monitor.NetworkMonitor
import kotlinx.coroutines.*
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * LocalVpnService - coroutine-powered VPN service running in foreground.
 * - Uses Notification channel "vpn_service_channel" (created in Application)
 * - Starts foreground with id = 1
 * - Establishes a TUN interface and processes I/O on Dispatchers.IO
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

        // Build and establish TUN interface
        try {
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
            } ?: run {
                // Could not establish VPN (user may have revoked); stop service
                stopSelf()
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            stopSelf()
        }

        return Service.START_STICKY
    }

    private suspend fun runVpnLoop(pfd: ParcelFileDescriptor) {
        val input = FileInputStream(pfd.fileDescriptor)
        val output = FileOutputStream(pfd.fileDescriptor)
        val buffer = ByteArray(32 * 1024)

        try {
            while (isActive) {
                val read = withContext(Dispatchers.IO) { input.read(buffer) }
                if (read > 0) {
                    NetworkMonitor.recordInbound(read.toLong())

                    // TODO: Insert packet processing/filtering logic here.
                    // For now we echo the bytes back (template). DON'T use this in production.
                    withContext(Dispatchers.IO) { output.write(buffer, 0, read) }
                    NetworkMonitor.recordOutbound(read.toLong())
                } else {
                    delay(50)
                }
            }
        } catch (ce: CancellationException) {
            // normal cancellation
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try { input.close() } catch (_: Exception) {}
            try { output.close() } catch (_: Exception) {}
        }
    }

    private fun startForegroundIfNeeded() {
        val notification = createNotification()
        // Use id = 1 as required
        startForeground(1, notification)
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, Class.forName("com.securitynav.security.ui.MainActivity"))
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, "vpn_service_channel")
            .setContentTitle("SecurityNav VPN")
            .setContentText("VPN is running")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        workerJob?.cancel()
        serviceScope.cancel()
        vpnInterface?.close()
        vpnInterface = null
        super.onDestroy()
    }

    override fun onRevoke() {
        // Called when the VPN interface is revoked by user/system
        onDestroy()
        super.onRevoke()
    }
}

package com.securitynav.security.vpn

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
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
 *
 * Behavior:
 * - If started with intent.extra "monitor_only" == true -> does NOT add default route (no device traffic interception).
 * - For full VPN interception (advanced), a proper userspace stack (tun2socks or native) must be integrated.
 */
class LocalVpnService : VpnService() {

    private val TAG = "LocalVpnService"
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var vpnInterface: ParcelFileDescriptor? = null
    private var workerJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate: LocalVpnService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand: starting VPN service (startId=$startId)")
        startForegroundIfNeeded()

        val monitorOnly = intent?.getBooleanExtra("monitor_only", true) ?: true
        Log.i(TAG, "VPN mode monitorOnly=$monitorOnly")

        // Build and establish TUN interface
        try {
            val builder = Builder()
                .setSession("SecurityNavVPN")
                .addAddress("10.0.0.2", 32)
                .setMtu(1500)

            if (!monitorOnly) {
                // Only add default route if not in monitor-only mode (advanced usage)
                builder.addRoute("0.0.0.0", 0)
            }

            vpnInterface?.close()
            vpnInterface = builder.establish()

            if (vpnInterface != null) {
                Log.i(TAG, "VPN interface established: ${vpnInterface}")
                workerJob?.cancel()
                workerJob = serviceScope.launch {
                    Log.i(TAG, "VPN loop: starting worker job")
                    runVpnLoop(vpnInterface!!, monitorOnly)
                    Log.i(TAG, "VPN loop: worker job finished")
                }
            } else {
                Log.e(TAG, "Failed to establish VPN interface: vpnInterface == null")
                // Could not establish VPN (user may have revoked); stop service
                stopSelf()
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Exception while establishing VPN: ${t.localizedMessage}", t)
            stopSelf()
        }

        return Service.START_STICKY
    }

    private suspend fun runVpnLoop(pfd: ParcelFileDescriptor, monitorOnly: Boolean) {
        Log.i(TAG, "runVpnLoop: entering loop (monitorOnly=$monitorOnly)")
        val input = FileInputStream(pfd.fileDescriptor)
        val output = FileOutputStream(pfd.fileDescriptor)
        val buffer = ByteArray(32 * 1024)

        try {
            while (coroutineContext.isActive) {
                val read = withContext(Dispatchers.IO) { input.read(buffer) }
                if (read > 0) {
                    Log.d(TAG, "runVpnLoop: read $read bytes")
                    NetworkMonitor.recordInbound(read.toLong())

                    // If monitorOnly we do minimal processing and don't block forwarding.
                    // For demo purposes echoing back; in monitorOnly mode this won't affect device connectivity since no default route.
                    withContext(Dispatchers.IO) { output.write(buffer, 0, read) }
                    NetworkMonitor.recordOutbound(read.toLong())
                } else {
                    delay(50)
                }
            }
        } catch (ce: CancellationException) {
            Log.i(TAG, "runVpnLoop: cancelled")
        } catch (e: Exception) {
            Log.e(TAG, "runVpnLoop: exception - ${e.localizedMessage}", e)
        } finally {
            try { input.close() } catch (ex: Exception) { Log.w(TAG, "runVpnLoop: error closing input: ${ex.localizedMessage}") }
            try { output.close() } catch (ex: Exception) { Log.w(TAG, "runVpnLoop: error closing output: ${ex.localizedMessage}") }
            Log.i(TAG, "runVpnLoop: exiting loop and cleaned resources")
        }
    }

    private fun startForegroundIfNeeded() {
        val notification = createNotification()
        // Use id = 1 as required
        Log.i(TAG, "startForeground: starting foreground with notification id=1")
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
        Log.i(TAG, "onDestroy: destroying LocalVpnService")
        workerJob?.cancel()
        serviceScope.cancel()
        try {
            vpnInterface?.close()
        } catch (e: Exception) {
            Log.w(TAG, "onDestroy: error closing vpnInterface: ${e.localizedMessage}")
        }
        vpnInterface = null
        super.onDestroy()
    }

    override fun onRevoke() {
        Log.w(TAG, "onRevoke: VPN interface revoked by user/system")
        // Called when the VPN interface is revoked by user/system
        onDestroy()
        super.onRevoke()
    }
}

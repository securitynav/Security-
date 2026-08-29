package com.securitynav.security.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.securitynav.security.services.SecurityMonitorService

class SystemEventReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            context?.let {
                val serviceIntent = Intent(it, SecurityMonitorService::class.java)
                it.startService(serviceIntent)
            }
        }
    }
}

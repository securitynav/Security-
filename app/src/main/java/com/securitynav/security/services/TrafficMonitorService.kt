package com.securitynav.security.services

import android.net.VpnService
import android.content.Intent

class TrafficMonitorService : VpnService() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
}

package com.securitynav.security.services

import android.app.Service
import android.content.Intent
import android.os.IBinder

class ScreenSpyAuditService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
}

package com.securitynav.security.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.securitynav.security.data.database.SecurityDatabase

class ScreenSpyAuditService : Service() {

    private lateinit var dbHelper: SecurityDatabase

    override fun onCreate() {
        super.onCreate()
        dbHelper = SecurityDatabase(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        auditScreenOverlayPermissions()
        return START_STICKY
    }

    private fun auditScreenOverlayPermissions() {
        dbHelper.logEvent(
            "SCREEN_SPY_AUDIT",
            "Auditoría de superposición de pantalla ejecutada correctamente."
        )
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

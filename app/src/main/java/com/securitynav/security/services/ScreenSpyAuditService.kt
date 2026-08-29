package com.securitynav.security.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.os.IBinder
import android.view.Display
import com.securitynav.security.data.database.SecurityDatabase

class ScreenSpyAuditService : Service() {

    private lateinit var displayManager: DisplayManager
    private lateinit var database: SecurityDatabase

    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) {
            checkDisplay(displayId, "PANTALLA_CONECTADA_ESPEJO")
        }

        override fun onDisplayRemoved(displayId: Int) {}

        override fun onDisplayChanged(displayId: Int) {
            checkDisplay(displayId, "CAMBIO_ESTADO_PANTALLA")
        }
    }

    override fun onCreate() {
        super.onCreate()
        database = SecurityDatabase(this)
        displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        displayManager.registerDisplayListener(displayListener, null)
    }

    private fun checkDisplay(displayId: Int, flag: String) {
        val display = displayManager.getDisplay(displayId) ?: return
        val isPresentation = (display.flags and Display.FLAG_PRESENTATION) != 0
        val isPrivate = (display.flags and Display.FLAG_PRIVATE) == 0

        if (isPresentation || isPrivate) {
            val db = database.getWritableEncryptedDatabase("master_key_temp")
            db.execSQL(
                "INSERT INTO security_logs (timestamp, event_type, details) VALUES (?, ?, ?)",
                arrayOf(System.currentTimeMillis(), flag, "Posible grabación o proyección remota detectada en displayId: $displayId")
            )
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        displayManager.unregisterDisplayListener(displayListener)
        super.onDestroy()
    }
}

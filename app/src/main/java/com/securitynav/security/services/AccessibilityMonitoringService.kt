package com.securitynav.security.services

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.securitynav.security.data.database.SecurityDatabase

class AccessibilityMonitoringService : AccessibilityService() {

    private lateinit var database: SecurityDatabase

    override fun onCreate() {
        super.onCreate()
        database = SecurityDatabase(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return

        val packageName = event.packageName?.toString() ?: "unknown"
        val eventType = event.eventType

        when (eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                logSuspiciousActivity("WINDOW_CHANGE", "Cambio de ventana detectado en: $packageName")
            }
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                Log.d("AccessibilityMonitor", "Click en vista por: $packageName")
            }
        }
    }

    private fun logSuspiciousActivity(type: String, details: String) {
        try {
            val db = database.getWritableEncryptedDatabase("master_key_temp")
            db.execSQL(
                "INSERT INTO security_logs (timestamp, event_type, details) VALUES (?, ?, ?)",
                arrayOf(System.currentTimeMillis(), type, details)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onInterrupt() {
        Log.w("AccessibilityMonitor", "Servicio de accesibilidad interrumpido")
    }
}

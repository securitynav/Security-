package com.securitynav.security.services

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.securitynav.security.data.database.SecurityDatabase

class AccessibilityMonitoringService : AccessibilityService() {

    private lateinit var dbHelper: SecurityDatabase

    override fun onCreate() {
        super.onCreate()
        dbHelper = SecurityDatabase(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let {
            val packageName = it.packageName?.toString() ?: "Desconocido"
            val eventType = AccessibilityEvent.eventTypeToString(it.eventType)

            if (it.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                dbHelper.logEvent(
                    "ACCESSIBILITY_EVENT",
                    "Cambio de ventana detectado en el paquete: $packageName (Evento: $eventType)"
                )
            }
        }
    }

    override fun onInterrupt() {}
}

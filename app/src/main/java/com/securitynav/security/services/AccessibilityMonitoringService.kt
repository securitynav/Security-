package com.securitynav.security.services

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class AccessibilityMonitoringService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
}

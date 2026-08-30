package com.securitynav.security.monitor

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.atomic.AtomicLong

/**
 * NetworkMonitor - lightweight in-memory counters for network metrics.
 * Exposes StateFlow to observe updates from UI.
 */
object NetworkMonitor {

    private val bytesIn = AtomicLong(0)
    private val bytesOut = AtomicLong(0)

    private val _totalBytesIn = MutableStateFlow(0L)
    val totalBytesIn: StateFlow<Long> = _totalBytesIn

    private val _totalBytesOut = MutableStateFlow(0L)
    val totalBytesOut: StateFlow<Long> = _totalBytesOut

    fun recordInbound(bytes: Long) {
        bytesIn.addAndGet(bytes)
        _totalBytesIn.value = bytesIn.get()
    }

    fun recordOutbound(bytes: Long) {
        bytesOut.addAndGet(bytes)
        _totalBytesOut.value = bytesOut.get()
    }

    fun reset() {
        bytesIn.set(0)
        bytesOut.set(0)
        _totalBytesIn.value = 0L
        _totalBytesOut.value = 0L
    }
}

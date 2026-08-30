package com.securitynav.security.data

import java.util.concurrent.CopyOnWriteArrayList

class SettingsRepository {
    private val settingsList = CopyOnWriteArrayList<String>(
        listOf("Protección de Red", "VPN Local", "SQLCipher Config", "Seguridad Avanzada")
    )

    fun getItems(): List<String> {
        return settingsList.toList()
    }

    fun updateItems(newItems: List<String>) {
        settingsList.clear()
        settingsList.addAll(newItems)
    }
}

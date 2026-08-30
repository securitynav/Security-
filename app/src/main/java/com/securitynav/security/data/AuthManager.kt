package com.securitynav.security.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * AuthManager - Responsible for local identity storage using EncryptedSharedPreferences.
 * Provides safe (null-free) accessors and avoids storing secrets in plaintext.
 */
class AuthManager private constructor(private val context: Context) {

    companion object {
        private const val PREF_FILE = "security_nav_auth"
        private const val KEY_USER_PIN = "user_pin"
        private const val KEY_DB_PASSPHRASE = "db_passphrase"

        @Volatile
        private var INSTANCE: AuthManager? = null

        fun getInstance(context: Context): AuthManager = INSTANCE ?: synchronized(this) {
            INSTANCE ?: AuthManager(context.applicationContext).also { INSTANCE = it }
        }
    }

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val prefs by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREF_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun savePin(pin: String) {
        prefs.edit().putString(KEY_USER_PIN, pin).apply()
    }

    fun getPin(): String? = prefs.getString(KEY_USER_PIN, null)

    fun clearPin() {
        prefs.edit().remove(KEY_USER_PIN).apply()
    }

    /**
     * Database passphrase management: generates or returns an existing passphrase stored securely.
     * This passphrase should be used to open SQLCipher databases. The passphrase itself is stored
     * encrypted in EncryptedSharedPreferences.
     */
    fun getOrCreateDbPassphrase(): String {
        val existing = prefs.getString(KEY_DB_PASSPHRASE, null)
        if (!existing.isNullOrBlank()) return existing

        val generated = java.util.UUID.randomUUID().toString().replace("-", "")
        prefs.edit().putString(KEY_DB_PASSPHRASE, generated).apply()
        return generated
    }
}

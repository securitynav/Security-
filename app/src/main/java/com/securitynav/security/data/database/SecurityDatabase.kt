package com.securitynav.security.data.database

import android.content.Context
import com.securitynav.security.data.security.KeyStoreManager
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteOpenHelper

class SecurityDatabase(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "security_nav_vault.db"
        private const val DATABASE_VERSION = 1
    }

    private val keyStoreManager = KeyStoreManager(context)

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS security_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                timestamp LONG NOT NULL,
                event_type TEXT NOT NULL,
                details TEXT NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS security_logs")
        onCreate(db)
    }

    fun getWritableEncryptedDatabase(): SQLiteDatabase {
        SQLiteDatabase.loadLibs(context)
        val passphrase = keyStoreManager.getMasterPassphrase()
        return getWritableDatabase(passphrase)
    }

    fun getReadableEncryptedDatabase(): SQLiteDatabase {
        SQLiteDatabase.loadLibs(context)
        val passphrase = keyStoreManager.getMasterPassphrase()
        return getReadableDatabase(passphrase)
    }

    fun logEvent(eventType: String, details: String) {
        try {
            val db = getWritableEncryptedDatabase()
            db.execSQL(
                "INSERT INTO security_logs (timestamp, event_type, details) VALUES (?, ?, ?)",
                arrayOf(System.currentTimeMillis(), eventType, details)
            )
            db.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

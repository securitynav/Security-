package com.securitynav.security.data.database

import android.content.Context
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteOpenHelper

class SecurityDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    init {
        SQLiteDatabase.loadLibs(context)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS security_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                timestamp LONG,
                event_type TEXT,
                details TEXT
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS security_logs")
        onCreate(db)
    }

    fun getWritableEncryptedDatabase(passphrase: String): SQLiteDatabase {
        return getWritableDatabase(passphrase)
    }

    fun getWritableEncryptedDatabase(passphrase: ByteArray): SQLiteDatabase {
        return getWritableDatabase(String(passphrase))
    }

    companion object {
        private const val DATABASE_NAME = "security_nav.db"
        private const val DATABASE_VERSION = 1
    }
}

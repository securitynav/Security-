package com.securitynav.security.data

import android.content.Context
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteOpenHelper
import net.sqlcipher.database.SQLiteDatabaseHook

/**
 * EncryptedDatabaseHelper - a thin wrapper to initialize and manage a SQLCipher encrypted DB.
 * It uses a passphrase provided by AuthManager.getOrCreateDbPassphrase().
 */
class EncryptedDatabaseHelper(
    context: Context,
    dbName: String = "securitynav.db",
    private val passphrase: String,
    version: Int = 1
) : SQLiteOpenHelper(context, dbName, null, version) {

    init {
        // Load SQLCipher native libraries
        SQLiteDatabase.loadLibs(context)
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Example table for traffic logs (basic, extend for production needs)
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS traffic (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                timestamp INTEGER NOT NULL,
                direction TEXT NOT NULL,
                bytes INTEGER NOT NULL,
                method TEXT,
                host TEXT
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS credentials (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                value TEXT NOT NULL
            );
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Migration path: implement schema migrations here
    }

    /**
     * Open writable database using the configured passphrase
     */
    fun openWritable(): SQLiteDatabase {
        // net.sqlcipher.database.SQLiteOpenHelper provides getWritableDatabase(passphraseCharArray)
        return getWritableDatabase(passphrase)
    }
}

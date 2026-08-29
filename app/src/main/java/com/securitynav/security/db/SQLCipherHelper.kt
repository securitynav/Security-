package com.securitynav.security.db

import android.content.Context
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.SQLiteOpenHelper

class SQLCipherHelper(context: Context) : SQLiteOpenHelper(
    context, 
    "securitynav_secure.db", 
    null, 
    1
) {
    init {
        SQLiteDatabase.loadLibs(context)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS traffic_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                timestamp TEXT,
                packet_type TEXT,
                destination TEXT,
                status TEXT
            )
        """)
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS user_prefs (
                key TEXT PRIMARY KEY,
                value TEXT
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS traffic_logs")
        db.execSQL("DROP TABLE IF EXISTS user_prefs")
        onCreate(db)
    }

    fun getSecureConnection(passphrase: String): SQLiteDatabase {
        return super.getWritableDatabase(passphrase)
    }
}

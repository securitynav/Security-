package com.securitynav.security.db

import android.content.Context
import net.zetetic.database.sqlite.SQLiteDatabase
import net.zetetic.database.sqlite.SQLiteOpenHelper

class SQLCipherHelper(context: Context) : SQLiteOpenHelper(context, "secure_nav.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS traffic_logs (id INTEGER PRIMARY KEY AUTOINCREMENT, data TEXT, timestamp INTEGER);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS traffic_logs")
        onCreate(db)
    }

    fun getSecureDatabase(passphrase: String): SQLiteDatabase {
        return super.getWritableDatabase(passphrase)
    }
}

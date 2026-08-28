package com.securitynav.security.data.database

import android.content.Context
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteOpenHelper

import android.content.Context
import net.zetetic.database.sqlcipher.SQLiteDatabase

class SecurityDatabase(context: Context) {
    init {
        SQLiteDatabase.loadLibs(context)
    }

    fun getWritableDatabase(passphrase: ByteArray): SQLiteDatabase {
        val dbFile = context.getDatabasePath("security_local_forensics.db")
        return SQLiteDatabase.openOrCreateDatabase(dbFile, passphrase, null, null)
    }
}

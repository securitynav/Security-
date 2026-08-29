package com.securitynav.security.ui

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.securitynav.security.R
import com.securitynav.security.data.database.SecurityDatabase

class SettingsActivity : AppCompatActivity() {

    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val prefs = getSharedPreferences("security_nav_config", Context.MODE_PRIVATE)

        val switchDeepHttp = findViewById<Switch>(R.id.switchDeepHttp)
        val switchUnity3d = findViewById<Switch>(R.id.switchUnity3d)
        val switchAutoBlock = findViewById<Switch>(R.id.switchAutoBlock)
        val btnClearDatabase = findViewById<Button>(R.id.btnClearDatabase)

        switchDeepHttp.isChecked = prefs.getBoolean("deep_http", true)
        switchUnity3d.isChecked = prefs.getBoolean("unity_3d", true)
        switchAutoBlock.isChecked = prefs.getBoolean("auto_block", false)

        switchDeepHttp.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("deep_http", isChecked).apply()
        }

        switchUnity3d.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("unity_3d", isChecked).apply()
        }

        switchAutoBlock.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("auto_block", isChecked).apply()
        }

        btnClearDatabase.setOnClickListener {
            try {
                val dbHelper = SecurityDatabase(this)
                val db = dbHelper.getWritableEncryptedDatabase()
                db.execSQL("DELETE FROM security_logs")
                db.close()
                Toast.makeText(this, "Base de datos cifrada limpiada correctamente.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Error al limpiar la base de datos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

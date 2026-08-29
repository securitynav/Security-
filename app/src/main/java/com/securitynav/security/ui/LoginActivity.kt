package com.securitynav.security.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.securitynav.security.R
import com.securitynav.security.data.database.SecurityDatabase

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate()
        setContentView(R.layout.activity_login)

        val etMasterPassword = findViewById<EditText>(R.id.etMasterPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val password = etMasterPassword.text.toString().trim()
            if (password.isNotEmpty()) {
                try {
                    val dbHelper = SecurityDatabase(this)
                    val db = dbHelper.getWritableEncryptedDatabase()
                    db.close()

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this, "Error al acceder a Android KeyStore Hardware", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Ingrese la clave de acceso", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

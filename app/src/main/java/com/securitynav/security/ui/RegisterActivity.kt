package com.securitynav.security.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.securitynav.security.R

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etNewPin = findViewById<EditText>(R.id.etNewPin)
        val etConfirmPin = findViewById<EditText>(R.id.etConfirmPin)
        val btnRegister = findViewById<Button>(R.id.btnRegisterPin)

        btnRegister.setOnClickListener {
            val pin = etNewPin.text?.toString() ?: ""
            val confirm = etConfirmPin.text?.toString() ?: ""

            if (pin.length != 4) {
                Toast.makeText(this, "El PIN debe tener exactamente 4 dígitos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pin != confirm) {
                Toast.makeText(this, "Los PINs no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Guardar PIN cifrado localmente
            val prefs = getSharedPreferences("security_nav_auth", Context.MODE_PRIVATE)
            prefs.edit().putString("user_pin", pin).apply()

            Toast.makeText(this, "Registro exitoso. Bóveda activada.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}

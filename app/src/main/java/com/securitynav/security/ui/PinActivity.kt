package com.securitynav.security.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.securitynav.security.R

class PinActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)

        val prefs = getSharedPreferences("security_nav_auth", Context.MODE_PRIVATE)
        val savedPin = prefs.getString("user_pin", null)

        val etPin = findViewById<EditText>(R.id.etPinCode)
        val btnLogin = findViewById<Button>(R.id.btnLoginPin)
        val btnRegister = findViewById<Button>(R.id.btnRegisterPin)
        val tvSubtitle = findViewById<TextView>(R.id.tvPinSubtitle)

        if (savedPin == null) {
            tvSubtitle.text = "Cree un PIN seguro de 4 dígitos"
            btnLogin.text = "GUARDAR Y INGRESAR"
        }

        btnLogin.setOnClickListener {
            val inputPin = etPin.text.toString()
            if (inputPin.length != 4) {
                Toast.makeText(this, "El PIN debe ser de 4 dígitos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (savedPin == null) {
                prefs.edit().putString("user_pin", inputPin).apply()
                Toast.makeText(this, "PIN registrado correctamente", Toast.LENGTH_SHORT).show()
                startMainActivity()
            } else if (inputPin == savedPin) {
                startMainActivity()
            } else {
                Toast.makeText(this, "PIN Incorrecto", Toast.LENGTH_SHORT).show()
            }
        }

        btnRegister.setOnClickListener {
            prefs.edit().remove("user_pin").apply()
            tvSubtitle.text = "Cree un PIN seguro de 4 dígitos"
            btnLogin.text = "GUARDAR Y INGRESAR"
            etPin.text.clear()
            Toast.makeText(this, "Ingrese el nuevo PIN", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

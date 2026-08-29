package com.securitynav.security.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.securitynav.security.R

class PinActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("security_nav_auth", Context.MODE_PRIVATE)
        val savedPin = prefs.getString("user_pin", null)

        if (savedPin.isNullOrEmpty()) {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_pin)

        val etPin = findViewById<EditText>(R.id.etPinInput)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val enteredPin = etPin.text?.toString() ?: ""
            if (enteredPin == savedPin) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "PIN Incorrecto", Toast.LENGTH_SHORT).show()
                etPin.setText("")
            }
        }
    }
}

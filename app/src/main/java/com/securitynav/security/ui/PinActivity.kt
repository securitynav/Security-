package com.securitynav.security.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.securitynav.security.data.AuthManager
import com.securitynav.security.databinding.ActivityPinBinding
import android.widget.Toast

class PinActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPinBinding
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authManager = AuthManager.getInstance(applicationContext)

        val savedPin = authManager.getPin()
        if (savedPin.isNullOrBlank()) {
            // No PIN registered -> go to register flow
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
            return
        }

        binding.btnLogin.setOnClickListener {
            val enteredPin = binding.etPinInput.text?.toString()?.trim() ?: ""
            if (enteredPin == savedPin) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "PIN Incorrecto", Toast.LENGTH_SHORT).show()
                binding.etPinInput.setText("")
            }
        }
    }
}

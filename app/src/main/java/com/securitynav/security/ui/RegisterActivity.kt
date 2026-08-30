package com.securitynav.security.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.securitynav.security.data.AuthManager
import com.securitynav.security.databinding.ActivityRegisterBinding
import com.google.android.material.textfield.TextInputEditText
import android.widget.Toast
import android.content.Context
import android.text.InputFilter

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authManager = AuthManager.getInstance(applicationContext)

        // Ensure inputs accept only digits and limit length to 4
        val digitFilter = InputFilter { source, _, _, _, _, _ ->
            if (source == null) return@InputFilter null
            if (source.toString().matches(Regex("^[0-9]*$"))) source else ""
        }
        binding.etNewPin.filters = arrayOf(digitFilter, InputFilter.LengthFilter(4))
        binding.etConfirmPin.filters = arrayOf(digitFilter, InputFilter.LengthFilter(4))

        binding.btnRegisterPin.setOnClickListener {
            val pin = binding.etNewPin.text?.toString()?.trim() ?: ""
            val confirm = binding.etConfirmPin.text?.toString()?.trim() ?: ""

            if (pin.length != 4) {
                Toast.makeText(this, "El PIN debe tener exactamente 4 dígitos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pin != confirm) {
                Toast.makeText(this, "Los PINs no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save securely using EncryptedSharedPreferences
            authManager.savePin(pin)

            Toast.makeText(this, "Registro exitoso. Bóveda activada.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}

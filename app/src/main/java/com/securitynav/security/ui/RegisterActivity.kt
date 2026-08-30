package com.securitynav.security.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.securitynav.security.R
import com.securitynav.security.data.AuthManager
import com.securitynav.security.databinding.ActivityRegisterBinding
import com.securitynav.security.utils.LottieCache

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authManager = AuthManager.getInstance(applicationContext)

        // Ensure inputs accept only digits and limit length to 4
        val digitFilter = android.text.InputFilter { source, _, _, _, _, _ ->
            if (source == null) return@InputFilter null
            if (source.toString().matches(Regex("^[0-9]*$"))) source else ""
        }
        binding.etNewPin.filters = arrayOf(digitFilter, android.text.InputFilter.LengthFilter(4))
        binding.etConfirmPin.filters = arrayOf(digitFilter, android.text.InputFilter.LengthFilter(4))

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

            // Play success animation then navigate
            try {
                LottieCache.getCachedComposition("lottie/success_animation.json")?.let {
                    binding.lottieSuccess.setComposition(it)
                } ?: binding.lottieSuccess.setAnimation("lottie/success_animation.json")
                binding.lottieSuccess.playAnimation()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            binding.postDelayed({
                Toast.makeText(this, "Registro exitoso. Bóveda activada.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }, 900)
        }
    }
}

package com.securitynav.security.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.securitynav.security.R

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val binding.lottieSuccess = findViewById<LottieAnimationView>(R.id.binding.lottieSuccess)
        binding.lottieSuccess?.visibility = View.VISIBLE
        binding.lottieSuccess?.playAnimation()

        Handler(Looper.getMainLooper()).postDelayed({
            // Siguiente pantalla o cierre
        }, 2000)
    }
}

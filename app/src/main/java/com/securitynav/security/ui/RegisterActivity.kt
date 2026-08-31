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

        val lottieSuccess = findViewById<LottieAnimationView?>(R.id.lottieSuccess)
        lottieSuccess?.visibility = View.VISIBLE
        lottieSuccess?.playAnimation()

        Handler(Looper.getMainLooper()).postDelayed({
            // Transición o cierre
        }, 2000)
    }
}

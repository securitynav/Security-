package com.securitynav.security.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.textfield.TextInputEditText
import com.securitynav.security.R
import com.securitynav.security.data.AuthManager
import com.securitynav.security.data.model.PromoRequest
import com.securitynav.security.data.remote.ApiClient
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        
        authManager = AuthManager(this)

        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnGoogleSignIn = findViewById<Button>(R.id.btnGoogleSignIn)
        val btnApplyPromo = findViewById<Button>(R.id.btnApplyPromo)
        val etPromoCode = findViewById<TextInputEditText>(R.id.etPromoCode)
        val tvPromoStatus = findViewById<TextView>(R.id.tvPromoStatus)
        val lottieSuccess = findViewById<LottieAnimationView>(R.id.lottieSuccess)

        // Mock Google Sign-In setup
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        
        btnApplyPromo.setOnClickListener {
            val code = etPromoCode.text?.toString()?.trim()
            if (!code.isNullOrEmpty()) {
                tvPromoStatus.visibility = View.VISIBLE
                tvPromoStatus.text = "Verifying..."
                tvPromoStatus.setTextColor(Color.parseColor("#B3B3B3")) // text_secondary
                
                lifecycleScope.launch {
                    try {
                        val request = PromoRequest(code = code, userId = "new_user")
                        val response = ApiClient.securityApiService.validatePromoCode(request)
                        if (response.isSuccessful && response.body() != null) {
                            val body = response.body()!!
                            if (body.isValid) {
                                tvPromoStatus.text = "Success: \${body.message} (-\${body.discountValue}%)"
                                tvPromoStatus.setTextColor(Color.parseColor("#39FF14")) // neon_green
                            } else {
                                tvPromoStatus.text = "Invalid Code: \${body.message}"
                                tvPromoStatus.setTextColor(Color.parseColor("#FF0055")) // neon_red
                            }
                        } else {
                            tvPromoStatus.text = "Server Error: Could not validate code"
                            tvPromoStatus.setTextColor(Color.parseColor("#FF0055")) // neon_red
                        }
                    } catch (e: Exception) {
                        // Normally handle network errors. For demo, mock it:
                        tvPromoStatus.text = "Promo applied successfully! (Mock)"
                        tvPromoStatus.setTextColor(Color.parseColor("#39FF14")) // neon_green
                    }
                }
            }
        }

        btnRegister.setOnClickListener {
            handleLogin(lottieSuccess)
        }

        btnGoogleSignIn.setOnClickListener {
            val signInIntent = mGoogleSignInClient.signInIntent
            @Suppress("DEPRECATION")
            startActivityForResult(signInIntent, 9001)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 9001) {
            val lottieSuccess = findViewById<LottieAnimationView>(R.id.lottieSuccess)
            handleLogin(lottieSuccess)
        }
    }

    private fun handleLogin(lottieSuccess: LottieAnimationView) {
        lottieSuccess.visibility = View.VISIBLE
        lottieSuccess.playAnimation()

        Handler(Looper.getMainLooper()).postDelayed({
            authManager.setLoggedIn(true)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 1500)
    }
}

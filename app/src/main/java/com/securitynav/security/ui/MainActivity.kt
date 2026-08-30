package com.securitynav.security.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.securitynav.security.R
import com.securitynav.security.notifications.SecurityNotificationManager

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_main)

            val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
            val btnOpenMenu = findViewById<ImageButton>(R.id.btnOpenMenu)
            val navView = findViewById<NavigationView>(R.id.navigationView)
            val btnMainLock = findViewById<CustomPulseLockView>(R.id.btnMainLock)
            val tvLockState = findViewById<TextView>(R.id.tvLockState)
            val btnViewCharts = findViewById<Button>(R.id.btnViewCharts)

            val notificationManager = SecurityNotificationManager(this)

            btnOpenMenu.setOnClickListener { drawerLayout.open() }

            navView.setNavigationItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.nav_dashboard -> startActivity(Intent(this, DashboardActivity::class.java))
                    R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
                    R.id.nav_faq -> startActivity(Intent(this, FaqActivity::class.java))
                    R.id.nav_logout -> {
                        getSharedPreferences("security_nav_auth", Context.MODE_PRIVATE)
                            .edit().remove("user_pin").apply()
                        startActivity(Intent(this, PinActivity::class.java))
                        finish()
                    }
                }
                drawerLayout.close()
                true
            }

            btnMainLock.setOnClickListener {
                when (btnMainLock.currentState) {
                    SecurityState.SECURE -> {
                        btnMainLock.setSecurityState(SecurityState.WARNING)
                        tvLockState.text = "ESTADO: ALERTA DE RIESGO"
                        tvLockState.setTextColor(android.graphics.Color.parseColor("#FFD600"))
                        notificationManager.sendSecurityAlert("Advertencia de Seguridad", "Superposición de pantalla detectada.")
                    }
                    SecurityState.WARNING -> {
                        btnMainLock.setSecurityState(SecurityState.DANGER)
                        tvLockState.text = "ESTADO: PELIGRO DETECTADO"
                        tvLockState.setTextColor(android.graphics.Color.parseColor("#FF3366"))
                        notificationManager.sendSecurityAlert("AMENAZA CRÍTICA", "Tráfico malicioso bloqueado.")
                    }
                    SecurityState.DANGER -> {
                        btnMainLock.setSecurityState(SecurityState.SECURE)
                        tvLockState.text = "ESTADO: PROTECCIÓN ACTIVA"
                        tvLockState.setTextColor(android.graphics.Color.parseColor("#00FF66"))
                        Toast.makeText(this, "Sistema Restablecido", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            btnViewCharts.setOnClickListener {
                startActivity(Intent(this, DashboardActivity::class.java))
            }

        } catch (e: Exception) {
            Toast.makeText(this, "CRASH DETECTADO: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
}

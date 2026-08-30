package com.securitynav.security.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.securitynav.security.R
import com.securitynav.security.databinding.ActivityMainBinding
import com.securitynav.security.notifications.SecurityNotificationManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var authManager: com.securitynav.security.data.AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authManager = com.securitynav.security.data.AuthManager.getInstance(applicationContext)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val btnOpenMenu = binding.btnOpenMenu
        val navView: NavigationView = binding.navigationView
        val btnMainLock = binding.btnMainLock
        val tvLockState = binding.tvLockState
        val btnViewCharts = binding.btnViewCharts

        val notificationManager = SecurityNotificationManager(this)

        btnOpenMenu.setOnClickListener { drawerLayout.open() }

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dashboard -> startActivity(Intent(this, DashboardActivity::class.java))
                R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
                R.id.nav_faq -> startActivity(Intent(this, FaqActivity::class.java))
                R.id.nav_logout -> {
                    authManager.clearPin()
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
                }
            }
        }

        btnViewCharts.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
        }
    }
}

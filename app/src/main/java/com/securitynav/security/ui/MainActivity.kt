package com.securitynav.security.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.securitynav.security.R
import com.securitynav.security.services.TrafficMonitorService

class MainActivity : AppCompatActivity() {

    private var isProtectionActive = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val btnOpenMenu = findViewById<ImageButton>(R.id.btnOpenMenu)
        val navView = findViewById<NavigationView>(R.id.navigationView)

        val btnMainLock = findViewById<RelativeLayout>(R.id.btnMainLock)
        val tvLockState = findViewById<TextView>(R.id.tvLockState)
        val btnViewCharts = findViewById<Button>(R.id.btnViewCharts)

        val switchVpnTraffic = findViewById<Switch>(R.id.switchVpnTraffic)

        btnOpenMenu.setOnClickListener {
            drawerLayout.open()
        }

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
            isProtectionActive = !isProtectionActive
            if (isProtectionActive) {
                tvLockState.text = "ESTADO: PROTECCIÓN ACTIVA"
                tvLockState.setTextColor(Color.parseColor("#00FF66"))
                btnMainLock.animate().rotationBy(360f).setDuration(500).start()
                startService(Intent(this, TrafficMonitorService::class.java))
                Toast.makeText(this, "Contramedidas de Seguridad Activadas", Toast.LENGTH_SHORT).show()
            } else {
                tvLockState.text = "ESTADO: PROTECCIÓN PAUSADA"
                tvLockState.setTextColor(Color.parseColor("#FF4444"))
                btnMainLock.animate().rotationBy(-360f).setDuration(500).start()
                stopService(Intent(this, TrafficMonitorService::class.java))
                Toast.makeText(this, "Contramedidas Desactivadas", Toast.LENGTH_SHORT).show()
            }
        }

        switchVpnTraffic.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                startService(Intent(this, TrafficMonitorService::class.java))
            } else {
                stopService(Intent(this, TrafficMonitorService::class.java))
            }
        }

        btnViewCharts.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
        }
    }
}

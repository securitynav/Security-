package com.securitynav.security.ui

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.navigation.NavigationView
import com.securitynav.security.R
import com.securitynav.security.data.AuthManager
import com.securitynav.security.databinding.ActivityMainBinding
import com.securitynav.security.notifications.SecurityNotificationManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var authManager: AuthManager
    private lateinit var vpnPrepareLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authManager = AuthManager.getInstance(applicationContext)

        // Register ActivityResultLauncher for VpnService.prepare() result
        vpnPrepareLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Permission granted
                startVpnService()
            } else {
                Toast.makeText(this, "Permiso VPN denegado", Toast.LENGTH_SHORT).show()
            }
        }

        val drawerLayout = binding.drawerLayout
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
                com.securitynav.security.ui.SecurityState.SECURE -> {
                    btnMainLock.setSecurityState(com.securitynav.security.ui.SecurityState.WARNING)
                    tvLockState.text = "ESTADO: ALERTA DE RIESGO"
                    tvLockState.setTextColor(android.graphics.Color.parseColor("#FFD600"))
                    notificationManager.sendSecurityAlert("Advertencia de Seguridad", "Superposición de pantalla detectada.")
                }
                com.securitynav.security.ui.SecurityState.WARNING -> {
                    btnMainLock.setSecurityState(com.securitynav.security.ui.SecurityState.DANGER)
                    tvLockState.text = "ESTADO: PELIGRO DETECTADO"
                    tvLockState.setTextColor(android.graphics.Color.parseColor("#FF3366"))
                    notificationManager.sendSecurityAlert("AMENAZA CRÍTICA", "Tráfico malicioso bloqueado.")
                }
                com.securitynav.security.ui.SecurityState.DANGER -> {
                    btnMainLock.setSecurityState(com.securitynav.security.ui.SecurityState.SECURE)
                    tvLockState.text = "ESTADO: PROTECCIÓN ACTIVA"
                    tvLockState.setTextColor(android.graphics.Color.parseColor("#00FF66"))
                }
            }
        }

        // Long-press to request VPN permission and start VPN service (non-intrusive demo)
        btnMainLock.setOnLongClickListener {
            requestVpnPermission()
            true
        }

        btnViewCharts.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
        }
    }

    private fun requestVpnPermission() {
        val prepareIntent = VpnService.prepare(this)
        if (prepareIntent != null) {
            vpnPrepareLauncher.launch(prepareIntent)
        } else {
            // Already have permission
            startVpnService()
        }
    }

    private fun startVpnService() {
        val svcIntent = Intent(this, com.securitynav.security.vpn.LocalVpnService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(svcIntent)
        } else {
            startService(svcIntent)
        }
    }
}

package com.securitynav.security.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import com.airbnb.lottie.LottieCompositionFactory
import com.eightbitlab.blurview.RenderScriptBlur
import com.eightbitlab.blurview.BlurView
import com.securitynav.security.R
import com.securitynav.security.data.AuthManager
import com.securitynav.security.databinding.ActivityMainBinding
import com.securitynav.security.monitor.NetworkMonitor
import com.securitynav.security.notifications.SecurityNotificationManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var authManager: AuthManager
    private lateinit var vpnPrepareLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestNotificationPermissionLauncher: ActivityResultLauncher<String>
    private var motionExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authManager = AuthManager.getInstance(applicationContext)

        // Preload Lottie compositions to avoid jank
        LottieCompositionFactory.fromAsset(this, "lottie/lock_animation.json").addListener { comp ->
            binding.lottieLock.setComposition(comp)
        }
        LottieCompositionFactory.fromAsset(this, "lottie/success_animation.json").addListener { comp ->
            try {
                binding.lottieLock.setProgress(0f)
            } catch (_: Exception) {}
        }

        // Initialize BlurView for glass effect
        initBlurView()

        // Register ActivityResultLauncher for VpnService.prepare() result
        vpnPrepareLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Permission granted
                startVpnService(monitorOnly = true)
            } else {
                Toast.makeText(this, "Permiso VPN denegado", Toast.LENGTH_SHORT).show()
            }
        }

        // Request notifications permission on Android 13+
        requestNotificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                Toast.makeText(this, "Permiso de notificaciones denegado; algunas alertas no se mostrarán", Toast.LENGTH_LONG).show()
            }
        }

        maybeRequestNotificationPermission()

        val drawerLayout = binding.drawerLayout
        val btnOpenMenu = binding.btnOpenMenu
        val navView = binding.navigationView
        val btnMainLock = binding.btnMainLock
        val tvLockState = binding.tvLockState
        val btnViewCharts = binding.btnViewCharts
        val motionLayout = findViewById<MotionLayout>(R.id.motionLayout)

        val notificationManager = SecurityNotificationManager(this)

        btnOpenMenu.setOnClickListener { drawerLayout.open() }

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dashboard -> startActivity(Intent(this, DashboardActivity::class.java))
                R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
                R.id.nav_faq -> startActivity(Intent(this, FaqActivity::class.java))
                R.id.nav_start_vpn -> requestVpnPermission()
                R.id.nav_stop_vpn -> stopService(Intent(this, com.securitynav.security.vpn.LocalVpnService::class.java))
                R.id.nav_diagnostics -> showDiagnostics()
                R.id.nav_about -> showAbout()
                R.id.nav_support -> showSupport()
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
            // bring Lottie to front and play
            try {
                binding.lottieLock.bringToFront()
                binding.lottieLock.playAnimation()
            } catch (_: Exception) {}

            // toggle motion layout expansion
            try {
                if (!motionExpanded) {
                    motionLayout.transitionToEnd()
                } else {
                    motionLayout.transitionToStart()
                }
                motionExpanded = !motionExpanded
            } catch (_: Exception) {}

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

        // Wire quick-action buttons
        binding.btnStartVpn.setOnClickListener { requestVpnPermission() }
        binding.btnStopVpn.setOnClickListener {
            stopService(Intent(this, com.securitynav.security.vpn.LocalVpnService::class.java))
            Toast.makeText(this, "Solicitud de parada de VPN enviada", Toast.LENGTH_SHORT).show()
        }
        binding.btnDiagnostics.setOnClickListener { showDiagnostics() }
        binding.btnAbout.setOnClickListener { showAbout() }

        btnViewCharts.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
        }

    }

    private fun initBlurView() {
        try {
            val blurView = findViewById<BlurView>(R.id.blurView)
            val rootView = findViewById<ViewGroup>(android.R.id.content).getChildAt(0) as ViewGroup
            val windowBackground = window.decorView.background
            val radius = 12f
            blurView.setupWith(rootView)
                .setFrameClearDrawable(windowBackground)
                .setBlurAlgorithm(RenderScriptBlur(this))
                .setBlurRadius(radius)
                .setOverlayColor(Color.parseColor("#1AFFFFFF"))
        } catch (e: Exception) {
            // Blur may fail on some devices, don't crash app
            e.printStackTrace()
        }
    }

    private fun maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun requestVpnPermission() {
        val prepareIntent = VpnService.prepare(this)
        if (prepareIntent != null) {
            vpnPrepareLauncher.launch(prepareIntent)
        } else {
            // Already have permission
            startVpnService(monitorOnly = true)
        }
    }

    private fun startVpnService(monitorOnly: Boolean) {
        val svcIntent = Intent(this, com.securitynav.security.vpn.LocalVpnService::class.java)
        svcIntent.putExtra("monitor_only", monitorOnly)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(svcIntent)
        } else {
            startService(svcIntent)
        }
    }

    private fun showDiagnostics() {
        val inBytes = NetworkMonitor.totalBytesIn.value
        val outBytes = NetworkMonitor.totalBytesOut.value
        val msg = "Bytes In: $inBytes\nBytes Out: $outBytes"
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Diagnósticos de Red")
            .setMessage(msg)
            .setPositiveButton("Cerrar", null)
            .show()
    }

    private fun showAbout() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Acerca de SecurityNav")
            .setMessage("SecurityNav v1.0\nProtección local ligera\nContacto: soporte@securitynav.local")
            .setPositiveButton("Cerrar", null)
            .show()
    }

    private fun showSupport() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Soporte y Reporte")
            .setMessage("Para soporte, por favor envía un informe desde la app o contacta soporte@securitynav.local")
            .setPositiveButton("Cerrar", null)
            .show()
    }
}

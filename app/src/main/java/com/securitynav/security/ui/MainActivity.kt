package com.securitynav.security.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.securitynav.security.R
import com.securitynav.security.engine.AntivirusEngine
import com.securitynav.security.notifications.NotificationHelper
import com.securitynav.security.services.SecurityMonitorService
import com.securitynav.security.services.TrafficMonitorService

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var notificationHelper: NotificationHelper
    private val VPN_REQUEST_CODE = 1001
    private val NOTIFICATION_PERMISSION_CODE = 1002

    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        notificationHelper = NotificationHelper(this)

        val btnStartVpn = findViewById<Button>(R.id.btnStartVpn)
        val btnScanAntivirus = findViewById<Button>(R.id.btnScanAntivirus)
        val btnOpenDashboard = findViewById<Button>(R.id.btnOpenDashboard)

        checkNotificationPermissions()

        val monitorIntent = Intent(this, SecurityMonitorService::class.java)
        startService(monitorIntent)

        btnStartVpn.setOnClickListener {
            val vpnIntent = VpnService.prepare(this)
            if (vpnIntent != null) {
                startActivityForResult(vpnIntent, VPN_REQUEST_CODE)
            } else {
                onActivityResult(VPN_REQUEST_CODE, RESULT_OK, null)
            }
        }

        btnScanAntivirus.setOnClickListener {
            runAntivirusScan()
        }

        btnOpenDashboard.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkNotificationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_CODE
                )
            }
        }
    }

    private fun runAntivirusScan() {
        tvStatus.text = "Escaneando aplicaciones instaladas..."
        val avEngine = AntivirusEngine(this)
        val threats = avEngine.scanInstalledApplications()

        if (threats.isEmpty()) {
            tvStatus.text = "Análisis Antivirus: 0 amenazas críticas encontradas."
            Toast.makeText(this, "Sistema Limpio", Toast.LENGTH_SHORT).show()
            notificationHelper.triggerWarningAlert("Escaneo Completo", "No se detectaron amenazas en el dispositivo.")
        } else {
            val criticalCount = threats.count { it.riskLevel == "CRITICAL" }
            tvStatus.text = "Atención: $criticalCount apps con riesgo crítico."
            
            notificationHelper.triggerCriticalAlert(
                "Amenaza Detectada",
                "Se encontraron $criticalCount aplicaciones sospechosas de superposición o permisos abusivos."
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK) {
            val intent = Intent(this, TrafficMonitorService::class.java)
            startService(intent)
            tvStatus.text = "Escudo VPN y Filtro de Tráfico ACTIVO"
            notificationHelper.triggerWarningAlert("Escudo VPN Activado", "Monitoreando conexiones entrantes y salientes.")
        }
    }
}

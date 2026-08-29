package com.securitynav.security.ui

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.securitynav.security.R
import com.securitynav.security.engine.AntivirusEngine
import com.securitynav.security.services.SecurityMonitorService
import com.securitynav.security.services.TrafficMonitorService

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private val VPN_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate()
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        val btnStartVpn = findViewById<Button>(R.id.btnStartVpn)
        val btnScanAntivirus = findViewById<Button>(R.id.btnScanAntivirus)
        val btnOpenDashboard = findViewById<Button>(R.id.btnOpenDashboard)

        // Iniciar servicio orquestador de seguridad en primer plano
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

    private fun runAntivirusScan() {
        tvStatus.text = "Escaneando aplicaciones instaladas..."
        val avEngine = AntivirusEngine(this)
        val threats = avEngine.scanInstalledApplications()

        if (threats.isEmpty()) {
            tvStatus.text = "Análisis Antivirus: 0 amenazas críticas encontradas."
            Toast.makeText(this, "Sistema Limpio", Toast.LENGTH_SHORT).show()
        } else {
            val criticalCount = threats.count { it.riskLevel == "CRITICAL" }
            tvStatus.text = "Atención: $criticalCount apps con riesgo crítico de permisos/overlay."
            Toast.makeText(this, "Amenazas Detectadas: $criticalCount", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK) {
            val intent = Intent(this, TrafficMonitorService::class.java)
            startService(intent)
            tvStatus.text = "Escudo VPN y Filtro de Tráfico ACTIVO"
        }
    }
}

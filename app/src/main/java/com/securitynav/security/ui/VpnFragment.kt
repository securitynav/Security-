package com.securitynav.security.ui

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.securitynav.security.R
import com.securitynav.security.vpn.LocalVpnService

class VpnFragment : Fragment() {
    
    private var isVpnRunning = false
    private lateinit var btnToggle: Button
    private lateinit var tvStatus: TextView

    private val vpnPrepareLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            startVpnService(monitorOnly = true)
        } else {
            Toast.makeText(requireContext(), "Permiso VPN denegado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_vpn, container, false)
        btnToggle = view.findViewById(R.id.btnToggleVpn) ?: view.findViewById<Button>(android.R.id.button1).apply { id = R.id.btnToggleVpn }
        tvStatus = view.findViewById(R.id.tvVpnStatus) ?: view.findViewById<TextView>(android.R.id.text1).apply { id = R.id.tvVpnStatus }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Find views explicitly based on fragment_vpn.xml (I'll need to rewrite the XML to match these IDs properly)
        
        btnToggle.setOnClickListener {
            if (isVpnRunning) {
                requireActivity().stopService(Intent(requireContext(), LocalVpnService::class.java))
                isVpnRunning = false
                updateUI()
            } else {
                requestVpnPermission()
            }
        }
        updateUI()
    }

    private fun requestVpnPermission() {
        val prepareIntent = VpnService.prepare(requireContext())
        if (prepareIntent != null) {
            vpnPrepareLauncher.launch(prepareIntent)
        } else {
            startVpnService(monitorOnly = true)
        }
    }

    private fun startVpnService(monitorOnly: Boolean) {
        val svcIntent = Intent(requireContext(), LocalVpnService::class.java)
        svcIntent.putExtra("monitor_only", monitorOnly)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireActivity().startForegroundService(svcIntent)
        } else {
            requireActivity().startService(svcIntent)
        }
        isVpnRunning = true
        updateUI()
    }
    
    private fun updateUI() {
        if (isVpnRunning) {
            tvStatus.text = "VPN Status: CONNECTED"
            tvStatus.setTextColor(android.graphics.Color.parseColor("#00FFC2")) // neon_cyan
            btnToggle.text = "Disconnect VPN"
            btnToggle.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FF0055")) // neon_red
        } else {
            tvStatus.text = "VPN Status: DISCONNECTED"
            tvStatus.setTextColor(android.graphics.Color.parseColor("#B3B3B3")) // text_secondary
            btnToggle.text = "Connect VPN"
            btnToggle.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#39FF14")) // neon_green
        }
    }
}

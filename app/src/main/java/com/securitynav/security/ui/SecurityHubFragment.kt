package com.securitynav.security.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.securitynav.security.R
import com.securitynav.security.monitor.NetworkMonitor

class SecurityHubFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_security_hub, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tvStatus = view.findViewById<TextView>(R.id.tvStatus)
        val tvBandwidth = view.findViewById<TextView>(R.id.tvBandwidth)
        
        tvStatus.text = "System Status: SECURE"
        
        // Mock bandwidth
        tvBandwidth.text = "Bandwidth: 1.2 MB/s | Scanning active"
    }
}

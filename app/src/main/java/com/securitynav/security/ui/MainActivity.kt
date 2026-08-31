package com.securitynav.security.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.securitynav.security.R
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderScriptBlur

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val btnSettings = findViewById<ImageButton>(R.id.btnSettings)
        
        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        
        // Setup BlurView for bottom navigation
        initBlurView()
        // Verify OTA updates
        com.securitynav.security.util.OtaUpdateManager(this).checkForUpdates()

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(SecurityHubFragment())
        }

        bottomNavigation.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_hub -> SecurityHubFragment()
                R.id.nav_analytics -> VulnerabilityDashboardFragment()
                R.id.nav_vpn -> VpnFragment()
                R.id.nav_guard -> GuardFragment()
                R.id.nav_vault -> VaultFragment()
                else -> SecurityHubFragment()
            }
            loadFragment(fragment)
            true
        }
    }
    
    private fun initBlurView() {
        // Verify OTA updates
        com.securitynav.security.util.OtaUpdateManager(this).checkForUpdates()
        try {
            val blurView = findViewById<BlurView>(R.id.bottomNavBlurView)
            val rootView = findViewById<ViewGroup>(android.R.id.content).getChildAt(0) as ViewGroup
            val windowBackground = window.decorView.background
            val radius = 12f
            blurView.setupWith(rootView, RenderScriptBlur(this))
                .setFrameClearDrawable(windowBackground)
                .setBlurRadius(radius)
                .setOverlayColor(Color.parseColor("#1AFFFFFF"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }
}

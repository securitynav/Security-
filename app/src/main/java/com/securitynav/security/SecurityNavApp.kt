package com.securitynav.security

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentCallbacks2
import android.content.res.Configuration
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import com.securitynav.security.utils.LottieCache

class SecurityNavApp : Application(), ComponentCallbacks2 {

    override fun onCreate() {
        super.onCreate()
        createVpnNotificationChannel()
        registerComponentCallbacks(this)
        initializeLottiePreload()
    }

    private fun createVpnNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "vpn_service_channel"
            val channelName = "SecurityNav VPN Service"
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT // default to allow subtle sounds
            ).apply {
                description = "Canal para notificaciones del servicio VPN en primer plano"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 100, 50)
                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                setSound(soundUri, audioAttributes)
            }
            (getSystemService(NotificationManager::class.java))?.createNotificationChannel(channel)
        }
    }

    private fun initializeLottiePreload() {
        try {
            LottieCache.preloadComposition(this, "lottie/lock_animation.json")
            LottieCache.preloadComposition(this, "lottie/success_animation.json")
        } catch (e: Exception) {
            // don't block app startup
            e.printStackTrace()
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        when (level) {
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE,
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW,
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> {
                LottieCache.trimCache()
            }
            ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN,
            ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> {
                LottieCache.clearCache()
            }
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        LottieCache.clearCache()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }
}

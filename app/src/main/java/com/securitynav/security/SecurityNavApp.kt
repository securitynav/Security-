package com.securitynav.security

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieCompositionFactory
import com.securitynav.security.util.LottieCache

class SecurityNavApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createVpnNotificationChannel()
        preloadLottieAnimations()
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

    private fun preloadLottieAnimations() {
        // Preload compositions asynchronously and cache them in LottieCache to reduce UI jank
        try {
            LottieCompositionFactory.fromAsset(this, "lottie/lock_animation.json")
                .addListener { composition: LottieComposition? ->
                    LottieCache.lock = composition
                }
                .addFailureListener { /* ignore preload failure */ }

            LottieCompositionFactory.fromAsset(this, "lottie/success_animation.json")
                .addListener { composition: LottieComposition? ->
                    LottieCache.success = composition
                }
                .addFailureListener { /* ignore preload failure */ }
        } catch (e: Exception) {
            // don't block app startup if preloading fails
            e.printStackTrace()
        }
    }
}

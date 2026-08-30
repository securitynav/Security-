package com.securitynav.security.util

import com.airbnb.lottie.LottieComposition

/**
 * Simple in-memory cache for preloaded Lottie compositions.
 * Kept minimal and thread-safe for quick access from Activities.
 */
object LottieCache {
    @Volatile
    var lock: LottieComposition? = null

    @Volatile
    var success: LottieComposition? = null
}

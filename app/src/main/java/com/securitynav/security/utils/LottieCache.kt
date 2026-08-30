package com.securitynav.security.utils

import android.content.Context
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieCompositionFactory
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

object LottieCache {
    private val cache = ConcurrentHashMap<String, WeakReference<LottieComposition>>()

    fun preloadComposition(context: Context, assetName: String) {
        val ref = cache[assetName]?.get()
        if (ref != null) return

        LottieCompositionFactory.fromAsset(context, assetName).addListener { composition ->
            cache[assetName] = WeakReference(composition)
        }
    }

    fun getCachedComposition(assetName: String): LottieComposition? {
        return cache[assetName]?.get()
    }

    fun trimCache() {
        val iterator = cache.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.value.get() == null) {
                iterator.remove()
            }
        }
    }

    fun clearCache() {
        cache.clear()
    }
}

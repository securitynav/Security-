package com.securitynav.security.ui.utils

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.graphics.drawable.Drawable
import com.eightbitlab.com.blurview.BlurView
import com.eightbitlab.com.blurview.RenderScriptBlur

object BlurHelper {

    fun setupBlurView(
        blurView: BlurView,
        decorView: ViewGroup,
        windowBackground: Drawable?,
        blurRadius: Float = 20f
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Use native RenderEffect on API 31+ for better performance
            try {
                // Hide the BlurView since we'll apply RenderEffect directly to the container
                blurView.visibility = View.GONE
                decorView.setRenderEffect(
                    RenderEffect.createBlurEffect(blurRadius, blurRadius, Shader.TileMode.CLAMP)
                )
            } catch (e: Exception) {
                // Fallback to BlurView if RenderEffect fails for any reason
                try {
                    blurView.setupWith(decorView, RenderScriptBlur(blurView.context))
                        .setBlurRadius(blurRadius)
                        .setFrameRate(60f)
                    windowBackground?.let { blurView.setWindowBackground(it) }
                } catch (ex: Exception) {
                    blurView.visibility = View.GONE
                }
            }
        } else {
            // Fallback safe path for older devices using RenderScriptBlur
            try {
                blurView.setupWith(decorView, RenderScriptBlur(blurView.context))
                    .setFrameRate(60f)
                    .setBlurRadius(blurRadius)

                windowBackground?.let {
                    blurView.setWindowBackground(it)
                }
            } catch (e: Exception) {
                // Disable blur on failure to avoid crashes
                blurView.visibility = View.GONE
            }
        }
    }
}

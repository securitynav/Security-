package com.securitynav.security.ui.utils

import android.view.View
import eightbitlab.com.blurview.blurview.BlurView
import eightbitlab.com.blurview.blurview.RenderScriptBlur

object BlurHelper {
    fun setupBlur(blurView: BlurView, rootView: View) {
        val radius = 20f
        val decorView = rootView.rootView
        val windowBackground = decorView.background

        blurView.setupWith(rootView, RenderScriptBlur(blurView.context))
            .setFrameRate(60f)
            .setBlurRadius(radius)
    }
}

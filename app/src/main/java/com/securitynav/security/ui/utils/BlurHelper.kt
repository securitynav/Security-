package com.securitynav.security.ui.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderScriptBlur

object BlurHelper {
    fun setupBlur(blurView: BlurView, rootView: ViewGroup, windowBackground: Drawable?, context: Context) {
        blurView.setupWith(rootView, RenderScriptBlur(context))
            .setFrameClearDrawable(windowBackground)
            .setBlurRadius(10f)
    }
}

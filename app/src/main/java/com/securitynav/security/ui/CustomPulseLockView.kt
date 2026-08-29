package com.securitynav.security.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator

enum class SecurityState {
    SECURE,    // Verde (#00FF66)
    WARNING,   // Amarillo (#FFD600)
    DANGER     // Rojo (#FF3366)
}

class CustomPulseLockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var currentState: SecurityState = SecurityState.SECURE
        private set

    private var waveRadius = 0f
    private var waveAlpha = 255

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val wavePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }

    private val waveAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 1200
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener { anim ->
            val fraction = anim.animatedValue as Float
            waveRadius = fraction * 50f
            waveAlpha = ((1f - fraction) * 255).toInt()
            invalidate()
        }
    }

    init {
        waveAnimator.start()
    }

    fun setSecurityState(state: SecurityState) {
        this.currentState = state
        when (state) {
            SecurityState.SECURE -> waveAnimator.duration = 1500
            SecurityState.WARNING -> waveAnimator.duration = 800
            SecurityState.DANGER -> waveAnimator.duration = 400
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val baseRadius = (Math.min(cx, cy) * 0.55f)

        val colorHex = when (currentState) {
            SecurityState.SECURE -> "#00FF66"
            SecurityState.WARNING -> "#FFD600"
            SecurityState.DANGER -> "#FF3366"
        }
        val mainColor = Color.parseColor(colorHex)

        // Onda expansiva animada
        wavePaint.color = mainColor
        wavePaint.alpha = waveAlpha
        canvas.drawCircle(cx, cy, baseRadius + waveRadius, wavePaint)

        // Círculo base
        fillPaint.color = mainColor
        fillPaint.style = Paint.Style.FILL
        canvas.drawCircle(cx, cy, baseRadius, fillPaint)
    }
}

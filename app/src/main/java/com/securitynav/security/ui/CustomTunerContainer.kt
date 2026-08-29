package com.securitynav.security.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.securitynav.security.databinding.CustomTunerContainerBinding

class CustomTunerContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: CustomTunerContainerBinding =
        CustomTunerContainerBinding.inflate(LayoutInflater.from(context), this, true)

    var onTuningChangedListener: ((Float) -> Unit)? = null

    init {
        setupTunerControls()
    }

    private fun setupTunerControls() {
        binding.tunerSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                onTuningChangedListener?.invoke(value)
            }
        }
    }

    fun setTunerRange(min: Float, max: Float, initial: Float) {
        binding.tunerSlider.valueFrom = min
        binding.tunerSlider.valueTo = max
        binding.tunerSlider.value = initial
    }
}

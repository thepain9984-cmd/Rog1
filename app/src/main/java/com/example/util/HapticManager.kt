package com.example.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.model.HapticStrength

class HapticManager(context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun vibrate(strength: HapticStrength) {
        if (strength == HapticStrength.OFF || vibrator == null || !vibrator.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val effect = when (strength) {
                    HapticStrength.LIGHT -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                    HapticStrength.MEDIUM -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                    HapticStrength.STRONG -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                    HapticStrength.OFF -> null
                }
                if (effect != null) {
                    vibrator.vibrate(effect)
                }
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(strength.durationMs)
            }
        } catch (e: Exception) {
            // Gracefully ignore if vibrator is unavailable
        }
    }
}

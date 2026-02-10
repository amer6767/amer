package com.echogardens.app.audio

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Manages haptic feedback for Echo Gardens.
 * Provides zen-like, calming vibration patterns.
 */
class HapticManager(context: Context) {
    
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    
    /**
     * Heartbeat-like vibration when harvesting light/completing focus.
     * Two gentle pulses like a heartbeat.
     */
    fun vibrateHeartbeat() {
        vibrator?.let { vib ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Pattern: pause, pulse, pause, pulse (heartbeat rhythm)
                val timings = longArrayOf(0, 80, 100, 80)
                val amplitudes = intArrayOf(0, 150, 0, 100)
                vib.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(longArrayOf(0, 80, 100, 80), -1)
            }
        }
    }
    
    /**
     * Soft tick for UI interactions.
     */
    fun vibrateTick() {
        vibrator?.let { vib ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vib.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createOneShot(10, 50))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(10)
            }
        }
    }
    
    /**
     * Celebration vibration for session complete.
     */
    fun vibrateSuccess() {
        vibrator?.let { vib ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Rising pattern for celebration
                val timings = longArrayOf(0, 50, 50, 50, 50, 100)
                val amplitudes = intArrayOf(0, 80, 0, 120, 0, 200)
                vib.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(longArrayOf(0, 50, 50, 50, 50, 100), -1)
            }
        }
    }
    
    /**
     * Gentle pulse for timer tick (optional, every minute).
     */
    fun vibrateGentlePulse() {
        vibrator?.let { vib ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createOneShot(30, 50))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(30)
            }
        }
    }
}

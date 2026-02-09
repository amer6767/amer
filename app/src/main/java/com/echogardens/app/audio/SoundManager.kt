package com.echogardens.app.audio

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.BatteryManager

/**
 * Manages ambient audio for Echo Gardens.
 * Features dynamic ASMR based on battery level as per the concept.
 */
class SoundManager(private val context: Context) {
    
    private var soundPool: SoundPool? = null
    private var windSoundId: Int = 0
    private var chimesSoundId: Int = 0
    private var cricketsSoundId: Int = 0
    
    private var isInitialized = false
    private var isMuted = false
    
    init {
        initializeSoundPool()
    }
    
    private fun initializeSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        
        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(audioAttributes)
            .build()
        
        // Note: In a real app, these would load from res/raw
        // For now, we'll use system sounds or generate tones
        isInitialized = true
    }
    
    /**
     * Get the current battery level (0-100).
     */
    fun getBatteryLevel(): Int {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level >= 0 && scale > 0) (level * 100) / scale else 50
    }
    
    /**
     * Determine ambient sound type based on battery level.
     * Full battery = vibrant breeze
     * Low battery = soft evening crickets
     */
    fun getAmbientType(): AmbientType {
        val battery = getBatteryLevel()
        return when {
            battery >= 70 -> AmbientType.VIBRANT_BREEZE
            battery >= 40 -> AmbientType.GENTLE_WIND
            battery >= 20 -> AmbientType.SOFT_EVENING
            else -> AmbientType.QUIET_CRICKETS
        }
    }
    
    /**
     * Mute/unmute audio.
     */
    fun setMuted(muted: Boolean) {
        isMuted = muted
        if (muted) {
            stopAllSounds()
        }
    }
    
    /**
     * Stop all playing sounds.
     */
    fun stopAllSounds() {
        soundPool?.autoPause()
    }
    
    /**
     * Release resources.
     */
    fun release() {
        soundPool?.release()
        soundPool = null
        isInitialized = false
    }
    
    enum class AmbientType {
        VIBRANT_BREEZE,   // Full battery - energetic nature sounds
        GENTLE_WIND,      // Medium battery - calm wind
        SOFT_EVENING,     // Low battery - twilight ambiance
        QUIET_CRICKETS    // Very low battery - night crickets
    }
}

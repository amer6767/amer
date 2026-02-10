package com.echogardens.app.engine

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "echo_gardens")

/**
 * The Aura Engine is the heart of Echo Gardens.
 * It manages the island state, tracks user consistency, and determines
 * whether the island is in Bloom or Mist phase.
 */
class AuraEngine(private val context: Context) {
    
    companion object {
        // DataStore keys
        private val KEY_STARDUST = longPreferencesKey("stardust")
        private val KEY_TOTAL_FOCUS_MINUTES = longPreferencesKey("total_focus_minutes")
        private val KEY_SESSIONS_COMPLETED = intPreferencesKey("sessions_completed")
        private val KEY_LAST_SESSION_TIME = longPreferencesKey("last_session_time")
        private val KEY_CONSECUTIVE_DAYS = intPreferencesKey("consecutive_days")
        private val KEY_LAST_ACTIVE_DATE = stringPreferencesKey("last_active_date")
        private val KEY_WORLD_TREE_LEVEL = intPreferencesKey("world_tree_level")
        
        // Category progress keys
        private val KEY_PROGRESS_WORK = floatPreferencesKey("progress_work")
        private val KEY_PROGRESS_HEALTH = floatPreferencesKey("progress_health")
        private val KEY_PROGRESS_LEARNING = floatPreferencesKey("progress_learning")
        private val KEY_PROGRESS_SOCIAL = floatPreferencesKey("progress_social")
        
        // Settings
        private val KEY_HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        private val KEY_SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
    }
    
    /**
     * Flow of the current island state, automatically updated from DataStore.
     */
    val islandState: Flow<IslandState> = context.dataStore.data.map { prefs ->
        val currentTime = System.currentTimeMillis()
        val baseState = IslandState(
            stardust = prefs[KEY_STARDUST] ?: 0L,
            totalFocusMinutes = prefs[KEY_TOTAL_FOCUS_MINUTES] ?: 0L,
            sessionsCompleted = prefs[KEY_SESSIONS_COMPLETED] ?: 0,
            lastSessionTimestamp = prefs[KEY_LAST_SESSION_TIME] ?: 0L,
            consecutiveDays = prefs[KEY_CONSECUTIVE_DAYS] ?: 0,
            worldTreeLevel = prefs[KEY_WORLD_TREE_LEVEL] ?: 1,
            categoryProgress = mapOf(
                FocusCategory.WORK to (prefs[KEY_PROGRESS_WORK] ?: 0f),
                FocusCategory.HEALTH to (prefs[KEY_PROGRESS_HEALTH] ?: 0f),
                FocusCategory.LEARNING to (prefs[KEY_PROGRESS_LEARNING] ?: 0f),
                FocusCategory.SOCIAL to (prefs[KEY_PROGRESS_SOCIAL] ?: 0f)
            )
        )
        
        // Determine aura phase based on activity
        val phase = if (baseState.shouldBloom(currentTime)) AuraPhase.BLOOM else AuraPhase.MIST
        baseState.copy(auraPhase = phase)
    }
    
    /**
     * Flow of settings
     */
    val hapticsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_HAPTICS_ENABLED] ?: true
    }
    
    val soundEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SOUND_ENABLED] ?: true
    }
    
    /**
     * Called when a focus session is completed.
     * Updates stardust, progress, and checks for level ups.
     */
    suspend fun onSessionComplete(session: FocusSession) {
        val reward = session.calculateReward()
        val currentTime = System.currentTimeMillis()
        
        context.dataStore.edit { prefs ->
            // Add stardust
            prefs[KEY_STARDUST] = (prefs[KEY_STARDUST] ?: 0L) + reward
            
            // Update focus time
            prefs[KEY_TOTAL_FOCUS_MINUTES] = (prefs[KEY_TOTAL_FOCUS_MINUTES] ?: 0L) + session.durationMinutes
            
            // Increment sessions
            prefs[KEY_SESSIONS_COMPLETED] = (prefs[KEY_SESSIONS_COMPLETED] ?: 0) + 1
            
            // Update last session time
            prefs[KEY_LAST_SESSION_TIME] = currentTime
            
            // Update category progress
            val progressKey = when (session.category) {
                FocusCategory.WORK -> KEY_PROGRESS_WORK
                FocusCategory.HEALTH -> KEY_PROGRESS_HEALTH
                FocusCategory.LEARNING -> KEY_PROGRESS_LEARNING
                FocusCategory.SOCIAL -> KEY_PROGRESS_SOCIAL
            }
            val currentProgress = prefs[progressKey] ?: 0f
            prefs[progressKey] = minOf(1f, currentProgress + (session.durationMinutes / 100f))
            
            // Check for world tree level up
            val totalMinutes = prefs[KEY_TOTAL_FOCUS_MINUTES] ?: 0L
            val newLevel = calculateTreeLevel(totalMinutes)
            prefs[KEY_WORLD_TREE_LEVEL] = newLevel
            
            // Update consecutive days
            updateConsecutiveDays(prefs, currentTime)
        }
    }
    
    /**
     * Calculate world tree level based on total focus time.
     */
    private fun calculateTreeLevel(totalMinutes: Long): Int {
        return when {
            totalMinutes >= 1000 -> 10
            totalMinutes >= 500 -> 8
            totalMinutes >= 250 -> 6
            totalMinutes >= 100 -> 4
            totalMinutes >= 50 -> 3
            totalMinutes >= 20 -> 2
            else -> 1
        }
    }
    
    /**
     * Update consecutive day tracking.
     */
    private fun updateConsecutiveDays(prefs: MutablePreferences, currentTime: Long) {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            .format(java.util.Date(currentTime))
        val lastActiveDate = prefs[KEY_LAST_ACTIVE_DATE]
        
        if (lastActiveDate == null) {
            prefs[KEY_CONSECUTIVE_DAYS] = 1
        } else if (lastActiveDate != today) {
            // Check if it's the next day
            val yesterday = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                .format(java.util.Date(currentTime - 24 * 60 * 60 * 1000))
            if (lastActiveDate == yesterday) {
                prefs[KEY_CONSECUTIVE_DAYS] = (prefs[KEY_CONSECUTIVE_DAYS] ?: 0) + 1
            } else {
                prefs[KEY_CONSECUTIVE_DAYS] = 1 // Reset streak
            }
        }
        prefs[KEY_LAST_ACTIVE_DATE] = today
    }
    
    /**
     * Toggle haptics setting.
     */
    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_HAPTICS_ENABLED] = enabled
        }
    }
    
    /**
     * Toggle sound setting.
     */
    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SOUND_ENABLED] = enabled
        }
    }
}

package com.echogardens.app.engine

/**
 * Represents the visual/emotional state of the island.
 * BLOOM = Active, vibrant, growing (user is consistent)
 * MIST = Calm, restful, foggy (user needs rest or hasn't been active)
 */
enum class AuraPhase {
    BLOOM,  // Bright colors, active wildlife, vibrant music
    MIST    // Calm fog, slow music, soft colors - encourages rest
}

/**
 * Categories for focus sessions that correspond to World Tree branches.
 */
enum class FocusCategory {
    WORK,
    HEALTH,
    LEARNING,
    SOCIAL
}

/**
 * Represents the complete state of the player's island.
 */
data class IslandState(
    val auraPhase: AuraPhase = AuraPhase.MIST,
    val stardust: Long = 0L,
    val totalFocusMinutes: Long = 0L,
    val sessionsCompleted: Int = 0,
    val lastSessionTimestamp: Long = 0L,
    val consecutiveDays: Int = 0,
    val categoryProgress: Map<FocusCategory, Float> = FocusCategory.values().associateWith { 0f },
    val worldTreeLevel: Int = 1
) {
    /**
     * Calculate growth level (0.0 to 1.0) based on total focus and consistency.
     */
    val growthProgress: Float
        get() = minOf(1f, totalFocusMinutes / 1000f)
    
    /**
     * Determine if the island should enter Bloom phase.
     * Bloom is active when user has been consistent (sessions in last 24h).
     */
    fun shouldBloom(currentTime: Long): Boolean {
        val hoursSinceLastSession = (currentTime - lastSessionTimestamp) / (1000 * 60 * 60)
        return hoursSinceLastSession < 24 && sessionsCompleted > 0
    }
}

/**
 * Represents an active focus session.
 */
data class FocusSession(
    val category: FocusCategory,
    val durationMinutes: Int,
    val startTime: Long = System.currentTimeMillis(),
    val isPaused: Boolean = false,
    val remainingMillis: Long = durationMinutes * 60 * 1000L
) {
    val isComplete: Boolean
        get() = remainingMillis <= 0
    
    /**
     * Calculate stardust reward based on duration and category.
     */
    fun calculateReward(): Long {
        val baseReward = durationMinutes * 2L
        return baseReward + (durationMinutes / 10) * 5 // Bonus for longer sessions
    }
}

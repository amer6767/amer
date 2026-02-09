package com.echogardens.app.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.echogardens.app.engine.AuraEngine
import com.echogardens.app.engine.FocusCategory
import com.echogardens.app.engine.FocusSession
import com.echogardens.app.engine.IslandState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for managing focus sessions.
 * Handles timer logic, pause/resume, and session completion.
 */
class FocusViewModel(private val auraEngine: AuraEngine) : ViewModel() {
    
    // Available focus durations in minutes
    val availableDurations = listOf(1, 2, 5, 15, 25, 45) // 1-2 min for testing
    
    // Current session state
    private val _currentSession = MutableStateFlow<FocusSession?>(null)
    val currentSession: StateFlow<FocusSession?> = _currentSession.asStateFlow()
    
    // Timer state
    private val _remainingMillis = MutableStateFlow(0L)
    val remainingMillis: StateFlow<Long> = _remainingMillis.asStateFlow()
    
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()
    
    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()
    
    // Session completion event
    private val _sessionCompleted = MutableSharedFlow<SessionResult>()
    val sessionCompleted: SharedFlow<SessionResult> = _sessionCompleted.asSharedFlow()
    
    // Island state
    val islandState: StateFlow<IslandState> = auraEngine.islandState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), IslandState())
    
    private var timerJob: Job? = null
    
    /**
     * Start a new focus session.
     */
    fun startSession(category: FocusCategory, durationMinutes: Int) {
        val session = FocusSession(
            category = category,
            durationMinutes = durationMinutes,
            startTime = System.currentTimeMillis()
        )
        _currentSession.value = session
        _remainingMillis.value = durationMinutes * 60 * 1000L
        _isRunning.value = true
        _isPaused.value = false
        
        startTimer()
    }
    
    /**
     * Pause the current session.
     */
    fun pauseSession() {
        _isPaused.value = true
        timerJob?.cancel()
    }
    
    /**
     * Resume the current session.
     */
    fun resumeSession() {
        _isPaused.value = false
        startTimer()
    }
    
    /**
     * Stop/cancel the current session without completing it.
     */
    fun stopSession() {
        timerJob?.cancel()
        _currentSession.value = null
        _isRunning.value = false
        _isPaused.value = false
        _remainingMillis.value = 0L
    }
    
    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_remainingMillis.value > 0 && !_isPaused.value) {
                delay(100) // Update every 100ms for smooth countdown
                _remainingMillis.value = (_remainingMillis.value - 100).coerceAtLeast(0)
                
                if (_remainingMillis.value <= 0) {
                    onSessionComplete()
                }
            }
        }
    }
    
    private suspend fun onSessionComplete() {
        val session = _currentSession.value ?: return
        _isRunning.value = false
        
        // Calculate reward
        val reward = session.calculateReward()
        
        // Update engine
        auraEngine.onSessionComplete(session)
        
        // Emit completion event
        _sessionCompleted.emit(
            SessionResult(
                category = session.category,
                durationMinutes = session.durationMinutes,
                stardustEarned = reward
            )
        )
        
        // Reset state
        _currentSession.value = null
    }
    
    /**
     * Format remaining time as MM:SS
     */
    fun formatTime(millis: Long): String {
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }
    
    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

/**
 * Result of a completed focus session.
 */
data class SessionResult(
    val category: FocusCategory,
    val durationMinutes: Int,
    val stardustEarned: Long
)

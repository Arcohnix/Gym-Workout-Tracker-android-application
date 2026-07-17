package com.example.forgegym.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.forgegym.data.models.CompletedSet
import com.example.forgegym.data.models.ExerciseRecommendation
import com.example.forgegym.data.models.WorkoutSession
import com.example.forgegym.data.repository.ProgressiveOverloadRepository
import com.example.forgegym.data.repository.WorkoutSessionRepository
import com.example.forgegym.util.haptic.HapticManager
import com.example.forgegym.util.haptic.HapticType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkoutSessionUiState(
    val session: WorkoutSession? = null,
    val weightInput: String = "",
    val repsInput: String = "",
    val noteInput: String = "",
    val lastPerformance: List<CompletedSet> = emptyList(),
    val recommendation: ExerciseRecommendation? = null,
    val restTimeRemaining: Int = 0,
    val isResting: Boolean = false,
    val isTimerPaused: Boolean = false,
    val elapsedTimeSeconds: Long = 0,
    val finishedSessionId: String? = null,
    val isFinished: Boolean = false,
    val isLoading: Boolean = false
)

sealed class WorkoutSessionUiEvent {
    data class OnWeightChanged(val weight: String) : WorkoutSessionUiEvent()
    data class OnRepsChanged(val reps: String) : WorkoutSessionUiEvent()
    data class OnNoteChanged(val note: String) : WorkoutSessionUiEvent()
    object OnCompleteSet : WorkoutSessionUiEvent()
    object OnToggleTimer : WorkoutSessionUiEvent()
    object OnSkipTimer : WorkoutSessionUiEvent()
    object OnTogglePauseSession : WorkoutSessionUiEvent()
    object OnNextExercise : WorkoutSessionUiEvent()
    object OnPreviousExercise : WorkoutSessionUiEvent()
    object OnSkipExercise : WorkoutSessionUiEvent()
    data class OnSelectExercise(val index: Int) : WorkoutSessionUiEvent()
    object OnFinishSession : WorkoutSessionUiEvent()
    object OnDiscardSession : WorkoutSessionUiEvent()
    data class OnDeleteSet(val setId: String) : WorkoutSessionUiEvent()
}

@HiltViewModel
class WorkoutSessionViewModel @Inject constructor(
    private val sessionRepository: WorkoutSessionRepository,
    private val progressiveOverloadRepository: ProgressiveOverloadRepository,
    private val hapticManager: HapticManager,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<WorkoutSessionUiState, WorkoutSessionUiEvent>(WorkoutSessionUiState(isLoading = true)) {

    private val sessionId: String? = savedStateHandle["sessionId"]
    private var timerJob: Job? = null
    private var elapsedTickerJob: Job? = null

    override val state: StateFlow<WorkoutSessionUiState> = combine(
        sessionRepository.activeSession,
        _state
    ) { session, currentUiState ->
        currentUiState.copy(
            session = session,
            isLoading = session == null && !currentUiState.isFinished
        )
    }.onEach { state ->
        checkRestTimerRecovery(state.session)
        startElapsedTicker(state.session)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WorkoutSessionUiState(isLoading = true)
    )

    init {
        viewModelScope.launch {
            // Wait for repository to check DB for active session
            delay(200) 
            
            val active = sessionRepository.activeSession.value
            
            if (active == null && sessionId != null) {
                // If still null, start a new session.
                sessionRepository.startSession(sessionId)
            }

            loadLastPerformance()
            updateState { it.copy(isLoading = false) }
        }
    }

    private var lastRecoveredSetId: String? = null

    private fun checkRestTimerRecovery(session: WorkoutSession?) {
        if (session == null || session.isPaused) return
        
        val currentExercise = session.exercises.getOrNull(session.currentExerciseIndex) ?: return
        val lastSet = currentExercise.sets.lastOrNull() ?: return
        
        if (lastRecoveredSetId != lastSet.id) {
            val now = System.currentTimeMillis()
            val elapsedSeconds = ((now - lastSet.timestamp) / 1000).toInt()
            val restDuration = 60 
            
            if (elapsedSeconds < restDuration && !_state.value.isResting) {
                startRestTimer(restDuration - elapsedSeconds)
            }
            lastRecoveredSetId = lastSet.id
        }
    }

    private fun startElapsedTicker(session: WorkoutSession?) {
        if (session == null || session.isPaused) {
            elapsedTickerJob?.cancel()
            return
        }
        if (elapsedTickerJob?.isActive == true) return

        elapsedTickerJob = viewModelScope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                val elapsedMillis = now - session.startTime - session.totalPausedDurationMillis
                updateState { it.copy(elapsedTimeSeconds = elapsedMillis / 1000) }
                delay(1000)
            }
        }
    }

    private suspend fun loadLastPerformance() {
        val session = sessionRepository.activeSession.value ?: return
        val exerciseId = session.exercises.getOrNull(session.currentExerciseIndex)?.exerciseId
        if (exerciseId != null) {
            val lastSets = sessionRepository.getLastPerformance(exerciseId)
            val recommendation = progressiveOverloadRepository.getRecommendationForExercise(exerciseId).first()
            updateState { it.copy(lastPerformance = lastSets, recommendation = recommendation) }
        }
    }

    override fun onEvent(event: WorkoutSessionUiEvent) {
        when (event) {
            is WorkoutSessionUiEvent.OnWeightChanged -> updateState { it.copy(weightInput = event.weight) }
            is WorkoutSessionUiEvent.OnRepsChanged -> updateState { it.copy(repsInput = event.reps) }
            is WorkoutSessionUiEvent.OnNoteChanged -> updateState { it.copy(noteInput = event.note) }
            WorkoutSessionUiEvent.OnCompleteSet -> completeSet()
            WorkoutSessionUiEvent.OnToggleTimer -> updateState { it.copy(isTimerPaused = !it.isTimerPaused) }
            WorkoutSessionUiEvent.OnSkipTimer -> {
                updateState { it.copy(isResting = false, restTimeRemaining = 0) }
                timerJob?.cancel()
            }
            WorkoutSessionUiEvent.OnTogglePauseSession -> {
                viewModelScope.launch { sessionRepository.togglePause() }
            }
            WorkoutSessionUiEvent.OnNextExercise -> {
                val current = state.value.session?.currentExerciseIndex ?: 0
                val total = state.value.session?.exercises?.size ?: 0
                if (current < total - 1) {
                    viewModelScope.launch {
                        sessionRepository.updateCurrentExerciseIndex(current + 1)
                        loadLastPerformance()
                    }
                }
            }
            WorkoutSessionUiEvent.OnPreviousExercise -> {
                val current = state.value.session?.currentExerciseIndex ?: 0
                if (current > 0) {
                    viewModelScope.launch {
                        sessionRepository.updateCurrentExerciseIndex(current - 1)
                        loadLastPerformance()
                    }
                }
            }
            is WorkoutSessionUiEvent.OnSelectExercise -> {
                viewModelScope.launch {
                    sessionRepository.updateCurrentExerciseIndex(event.index)
                    loadLastPerformance()
                }
            }
            WorkoutSessionUiEvent.OnSkipExercise -> {
                onEvent(WorkoutSessionUiEvent.OnNextExercise)
            }
            WorkoutSessionUiEvent.OnFinishSession -> {
                viewModelScope.launch {
                    val currentSessionId = state.value.session?.id
                    if (currentSessionId != null) {
                        sessionRepository.finishSession(_state.value.noteInput)
                        hapticManager.vibrate(HapticType.SUCCESS)
                        updateState { it.copy(isFinished = true, finishedSessionId = currentSessionId) }
                    }
                }
            }
            WorkoutSessionUiEvent.OnDiscardSession -> {
                viewModelScope.launch {
                    sessionRepository.discardSession()
                    updateState { it.copy(isFinished = true) }
                }
            }
            is WorkoutSessionUiEvent.OnDeleteSet -> {
                viewModelScope.launch {
                    sessionRepository.removeSet(event.setId)
                }
            }
        }
    }

    private fun completeSet() {
        val currentState = state.value
        val weight = currentState.weightInput.toDoubleOrNull() ?: 0.0
        val reps = currentState.repsInput.toIntOrNull() ?: 0
        val note = currentState.noteInput
        
        val session = currentState.session ?: return
        val exerciseId = session.exercises.getOrNull(session.currentExerciseIndex)?.exerciseId ?: return

        viewModelScope.launch {
            try {
                sessionRepository.logSet(exerciseId, weight, reps, note, false)
                updateState { it.copy(noteInput = "") } 
                hapticManager.vibrate(HapticType.IMPACT)
                startRestTimer(60) 
            } catch (e: Exception) {
            }
        }
    }

    private fun startRestTimer(seconds: Int) {
        timerJob?.cancel()
        updateState { it.copy(isResting = true, isTimerPaused = false, restTimeRemaining = seconds) }
        timerJob = viewModelScope.launch {
            var remaining = seconds
            while (remaining > 0) {
                if (!_state.value.isTimerPaused) {
                    delay(1000)
                    remaining--
                    updateState { it.copy(restTimeRemaining = remaining) }
                } else {
                    delay(100) 
                }
            }
            updateState { it.copy(isResting = false) }
        }
    }
}

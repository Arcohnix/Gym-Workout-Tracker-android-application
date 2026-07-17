package com.example.forgegym.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.forgegym.data.models.WorkoutSummary
import com.example.forgegym.data.repository.HistoryRepository
import com.example.forgegym.data.repository.WorkoutSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkoutSummaryUiState(
    val summary: WorkoutSummary? = null,
    val isRepeating: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class WorkoutSummaryUiEvent {
    object OnFinishClick : WorkoutSummaryUiEvent()
    object OnShareClick : WorkoutSummaryUiEvent()
    object OnRepeatWorkout : WorkoutSummaryUiEvent()
}

@HiltViewModel
class WorkoutSummaryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val sessionRepository: WorkoutSessionRepository,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<WorkoutSummaryUiState, WorkoutSummaryUiEvent>(WorkoutSummaryUiState(isLoading = true)) {

    private val sessionId: String = checkNotNull(savedStateHandle["sessionId"])

    override val state: StateFlow<WorkoutSummaryUiState> = historyRepository.getWorkoutSummary(sessionId)
        .map { summary ->
            if (summary == null) {
                WorkoutSummaryUiState(isLoading = false, error = "Summary details not found for session ID: $sessionId")
            } else {
                WorkoutSummaryUiState(
                    summary = summary,
                    isLoading = false
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WorkoutSummaryUiState(isLoading = true)
        )

    override fun onEvent(event: WorkoutSummaryUiEvent) {
        when (event) {
            WorkoutSummaryUiEvent.OnFinishClick -> { }
            WorkoutSummaryUiEvent.OnShareClick -> { }
            WorkoutSummaryUiEvent.OnRepeatWorkout -> {
                val summary = state.value.summary ?: return
                viewModelScope.launch {
                    sessionRepository.startSession(summary.workoutId)
                    updateState { it.copy(isRepeating = true) }
                }
            }
        }
    }
}

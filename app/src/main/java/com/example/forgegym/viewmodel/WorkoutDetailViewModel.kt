package com.example.forgegym.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.forgegym.data.models.Workout
import com.example.forgegym.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkoutDetailUiState(
    val workout: Workout? = null,
    val isLoading: Boolean = false,
    val isDeleted: Boolean = false,
    val error: String? = null
)

sealed class WorkoutDetailUiEvent {
    object OnStartWorkout : WorkoutDetailUiEvent()
    object OnDeleteWorkout : WorkoutDetailUiEvent()
    object OnDuplicateWorkout : WorkoutDetailUiEvent()
}

@HiltViewModel
class WorkoutDetailViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<WorkoutDetailUiState, WorkoutDetailUiEvent>(WorkoutDetailUiState(isLoading = true)) {

    private val workoutId: String = checkNotNull(savedStateHandle["workoutId"])

    override val state: StateFlow<WorkoutDetailUiState> = workoutRepository.getWorkoutById(workoutId)
        .map { workout ->
            WorkoutDetailUiState(
                workout = workout,
                isLoading = false,
                error = if (workout == null && !_state.value.isDeleted) "Workout not found" else null,
                isDeleted = _state.value.isDeleted
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WorkoutDetailUiState(isLoading = true)
        )

    override fun onEvent(event: WorkoutDetailUiEvent) {
        when (event) {
            WorkoutDetailUiEvent.OnStartWorkout -> {
                // To be handled by navigation
            }
            WorkoutDetailUiEvent.OnDeleteWorkout -> {
                viewModelScope.launch {
                    workoutRepository.deleteWorkout(workoutId)
                    updateState { it.copy(isDeleted = true) }
                }
            }
            WorkoutDetailUiEvent.OnDuplicateWorkout -> {
                viewModelScope.launch {
                    workoutRepository.duplicateWorkout(workoutId)
                }
            }
        }
    }
}

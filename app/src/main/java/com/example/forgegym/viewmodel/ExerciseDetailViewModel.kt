package com.example.forgegym.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.forgegym.data.models.Exercise
import com.example.forgegym.data.repository.ExerciseHistoryStats
import com.example.forgegym.data.repository.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExerciseDetailUiState(
    val exercise: Exercise? = null,
    val stats: ExerciseHistoryStats = ExerciseHistoryStats(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class ExerciseDetailUiEvent {
    data class OnToggleFavorite(val isFavorite: Boolean) : ExerciseDetailUiEvent()
}

@HiltViewModel
class ExerciseDetailViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<ExerciseDetailUiState, ExerciseDetailUiEvent>(ExerciseDetailUiState(isLoading = true)) {

    private val exerciseId: String = checkNotNull(savedStateHandle["exerciseId"])

    override val state: StateFlow<ExerciseDetailUiState> = combine(
        exerciseRepository.getExerciseById(exerciseId),
        exerciseRepository.getExerciseHistoryStats(exerciseId)
    ) { exercise, stats ->
        ExerciseDetailUiState(
            exercise = exercise,
            stats = stats,
            isLoading = false,
            error = if (exercise == null) "Exercise not found" else null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExerciseDetailUiState(isLoading = true)
    )

    override fun onEvent(event: ExerciseDetailUiEvent) {
        when (event) {
            is ExerciseDetailUiEvent.OnToggleFavorite -> {
                viewModelScope.launch {
                    exerciseRepository.updateFavoriteStatus(exerciseId, event.isFavorite)
                }
            }
        }
    }
}

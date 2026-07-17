package com.example.forgegym.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.forgegym.data.models.Workout
import com.example.forgegym.data.models.WorkoutCategory
import com.example.forgegym.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class WorkoutLibraryUiState(
    val workouts: List<Workout> = emptyList(),
    val categories: List<WorkoutCategory> = WorkoutCategory.entries.filter { it != WorkoutCategory.ALL },
    val selectedCategory: WorkoutCategory = WorkoutCategory.ALL,
    val searchQuery: String = "",
    val isLoading: Boolean = false
)

sealed class WorkoutLibraryUiEvent {
    data class OnCategorySelected(val category: WorkoutCategory) : WorkoutLibraryUiEvent()
    data class OnSearchQueryChanged(val query: String) : WorkoutLibraryUiEvent()
    data class OnWorkoutClick(val workoutId: String) : WorkoutLibraryUiEvent()
}

@HiltViewModel
class WorkoutLibraryViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository
) : BaseViewModel<WorkoutLibraryUiState, WorkoutLibraryUiEvent>(WorkoutLibraryUiState(isLoading = true)) {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow(WorkoutCategory.ALL)

    override val state: StateFlow<WorkoutLibraryUiState> = combine(
        workoutRepository.getAllWorkouts(),
        _searchQuery,
        _selectedCategory
    ) { workouts, query, category ->
        val filteredWorkouts = workouts.filter { workout ->
            val matchesQuery = workout.name.contains(query, ignoreCase = true) ||
                               workout.description.contains(query, ignoreCase = true)
            val matchesCategory = category == WorkoutCategory.ALL || workout.category == category
            matchesQuery && matchesCategory
        }
        
        WorkoutLibraryUiState(
            workouts = filteredWorkouts,
            selectedCategory = category,
            searchQuery = query,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WorkoutLibraryUiState(isLoading = true)
    )

    override fun onEvent(event: WorkoutLibraryUiEvent) {
        when (event) {
            is WorkoutLibraryUiEvent.OnCategorySelected -> {
                _selectedCategory.value = event.category
            }
            is WorkoutLibraryUiEvent.OnSearchQueryChanged -> {
                _searchQuery.value = event.query
            }
            is WorkoutLibraryUiEvent.OnWorkoutClick -> { /* Navigation handled in UI */ }
        }
    }
}

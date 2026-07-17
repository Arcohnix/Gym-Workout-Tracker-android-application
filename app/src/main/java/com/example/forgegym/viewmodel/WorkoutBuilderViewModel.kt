package com.example.forgegym.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.forgegym.data.models.*
import com.example.forgegym.data.repository.ExerciseRepository
import com.example.forgegym.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class WorkoutBuilderUiState(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val description: String = "",
    val category: WorkoutCategory = WorkoutCategory.CUSTOM,
    val difficulty: Difficulty = Difficulty.INTERMEDIATE,
    val exercises: List<WorkoutExercise> = emptyList(),
    val availableExercises: List<Exercise> = emptyList(),
    val recentlyUsedExercises: List<Exercise> = emptyList(),
    val exerciseSearchQuery: String = "",
    val selectedMuscleGroup: MuscleGroup = MuscleGroup.ALL,
    val isSaving: Boolean = false,
    val isFinished: Boolean = false,
    val isLoading: Boolean = false,
    val isEditMode: Boolean = false
) {
    val filteredAvailableExercises: List<Exercise>
        get() {
            // Combine recently used (at the top) and then the rest, filtered
            val filtered = availableExercises.filter { exercise ->
                val matchesQuery = exercise.name.contains(exerciseSearchQuery, ignoreCase = true)
                val matchesMuscle = selectedMuscleGroup == MuscleGroup.ALL || exercise.primaryMuscleGroup == selectedMuscleGroup
                matchesQuery && matchesMuscle
            }
            
            if (exerciseSearchQuery.isEmpty() && selectedMuscleGroup == MuscleGroup.ALL) {
                val recentIds = recentlyUsedExercises.map { it.id }.toSet()
                return recentlyUsedExercises + filtered.filter { it.id !in recentIds }
            }
            return filtered
        }
}

sealed class WorkoutBuilderUiEvent {
    data class OnNameChanged(val name: String) : WorkoutBuilderUiEvent()
    data class OnDescriptionChanged(val description: String) : WorkoutBuilderUiEvent()
    data class OnCategoryChanged(val category: WorkoutCategory) : WorkoutBuilderUiEvent()
    data class OnDifficultyChanged(val difficulty: Difficulty) : WorkoutBuilderUiEvent()
    data class OnAddExercise(val exercise: Exercise) : WorkoutBuilderUiEvent()
    data class OnRemoveExercise(val workoutExerciseId: String) : WorkoutBuilderUiEvent()
    data class OnReorderExercises(val fromIndex: Int, val toIndex: Int) : WorkoutBuilderUiEvent()
    data class OnRestTimeChanged(val workoutExerciseId: String, val seconds: Int) : WorkoutBuilderUiEvent()
    data class OnNoteChanged(val workoutExerciseId: String, val note: String) : WorkoutBuilderUiEvent()
    data class OnExerciseSearchQueryChanged(val query: String) : WorkoutBuilderUiEvent()
    data class OnMuscleGroupSelected(val muscleGroup: MuscleGroup) : WorkoutBuilderUiEvent()
    data class OnToggleFavoriteExercise(val exerciseId: String, val isFavorite: Boolean) : WorkoutBuilderUiEvent()
    object OnSaveWorkout : WorkoutBuilderUiEvent()
    object OnDeleteWorkout : WorkoutBuilderUiEvent()
}

@HiltViewModel
class WorkoutBuilderViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<WorkoutBuilderUiState, WorkoutBuilderUiEvent>(WorkoutBuilderUiState()) {

    private val workoutId: String? = savedStateHandle["workoutId"]

    init {
        viewModelScope.launch {
            val exercisesFlow = exerciseRepository.getAllExercises()
            val recentFlow = exerciseRepository.getRecentlyUsedExercises()
            
            combine(exercisesFlow, recentFlow) { all, recent ->
                updateState { it.copy(availableExercises = all, recentlyUsedExercises = recent) }
            }.first()

            if (workoutId != null) {
                updateState { it.copy(isLoading = true, isEditMode = true) }
                val workout = workoutRepository.getWorkoutById(workoutId).first()
                if (workout != null) {
                    updateState { 
                        it.copy(
                            id = workout.id,
                            name = workout.name,
                            description = workout.description,
                            category = workout.category,
                            difficulty = workout.difficulty,
                            exercises = workout.workoutExercises,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    override fun onEvent(event: WorkoutBuilderUiEvent) {
        when (event) {
            is WorkoutBuilderUiEvent.OnNameChanged -> updateState { it.copy(name = event.name) }
            is WorkoutBuilderUiEvent.OnDescriptionChanged -> updateState { it.copy(description = event.description) }
            is WorkoutBuilderUiEvent.OnCategoryChanged -> updateState { it.copy(category = event.category) }
            is WorkoutBuilderUiEvent.OnDifficultyChanged -> updateState { it.copy(difficulty = event.difficulty) }
            is WorkoutBuilderUiEvent.OnAddExercise -> addExercise(event.exercise)
            is WorkoutBuilderUiEvent.OnRemoveExercise -> removeExercise(event.workoutExerciseId)
            is WorkoutBuilderUiEvent.OnReorderExercises -> reorderExercises(event.fromIndex, event.toIndex)
            is WorkoutBuilderUiEvent.OnRestTimeChanged -> updateRestTime(event.workoutExerciseId, event.seconds)
            is WorkoutBuilderUiEvent.OnNoteChanged -> updateNote(event.workoutExerciseId, event.note)
            is WorkoutBuilderUiEvent.OnExerciseSearchQueryChanged -> updateState { it.copy(exerciseSearchQuery = event.query) }
            is WorkoutBuilderUiEvent.OnMuscleGroupSelected -> updateState { it.copy(selectedMuscleGroup = event.muscleGroup) }
            is WorkoutBuilderUiEvent.OnToggleFavoriteExercise -> toggleFavorite(event.exerciseId, event.isFavorite)
            WorkoutBuilderUiEvent.OnSaveWorkout -> saveWorkout()
            WorkoutBuilderUiEvent.OnDeleteWorkout -> deleteWorkout()
        }
    }

    private fun addExercise(exercise: Exercise) {
        updateState { state ->
            val newExercise = WorkoutExercise(
                exercise = exercise,
                order = state.exercises.size
            )
            state.copy(exercises = state.exercises + newExercise)
        }
    }

    private fun removeExercise(id: String) {
        updateState { state ->
            val updated = state.exercises.filter { it.id != id }
                .mapIndexed { index, ex -> ex.copy(order = index) }
            state.copy(exercises = updated)
        }
    }

    private fun reorderExercises(from: Int, to: Int) {
        updateState { state ->
            val list = state.exercises.toMutableList()
            if (from in list.indices && to in list.indices) {
                val item = list.removeAt(from)
                list.add(to, item)
                state.copy(exercises = list.mapIndexed { index, ex -> ex.copy(order = index) })
            } else state
        }
    }

    private fun updateRestTime(id: String, seconds: Int) {
        updateState { state ->
            val updated = state.exercises.map { 
                if (it.id == id) it.copy(restTimeSeconds = seconds) else it
            }
            state.copy(exercises = updated)
        }
    }

    private fun updateNote(id: String, note: String) {
        updateState { state ->
            val updated = state.exercises.map { 
                if (it.id == id) it.copy(note = note) else it
            }
            state.copy(exercises = updated)
        }
    }

    private fun toggleFavorite(exerciseId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            exerciseRepository.updateFavoriteStatus(exerciseId, isFavorite)
            // Trigger a re-fetch or rely on the Flow if we were observing properly.
            // Since init combined and first(), let's just re-fetch manually for simplicity here or better observe.
            val exercises = exerciseRepository.getAllExercises().first()
            updateState { it.copy(availableExercises = exercises) }
        }
    }

    private fun saveWorkout() {
        val currentState = state.value
        if (currentState.name.isBlank()) return

        viewModelScope.launch {
            updateState { it.copy(isSaving = true) }
            val workout = Workout(
                id = currentState.id,
                name = currentState.name,
                description = currentState.description,
                category = currentState.category,
                difficulty = currentState.difficulty,
                workoutExercises = currentState.exercises,
                estimatedDurationMinutes = currentState.exercises.size * 10 
            )
            workoutRepository.saveWorkout(workout)
            updateState { it.copy(isSaving = false, isFinished = true) }
        }
    }

    private fun deleteWorkout() {
        workoutId?.let {
            viewModelScope.launch {
                workoutRepository.deleteWorkout(it)
                updateState { it.copy(isFinished = true) }
            }
        }
    }
}

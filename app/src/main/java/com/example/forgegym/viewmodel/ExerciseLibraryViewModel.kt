package com.example.forgegym.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.forgegym.data.models.*
import com.example.forgegym.data.repository.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExerciseLibraryUiState(
    val filteredExercises: List<Exercise> = emptyList(),
    val recentlyUsed: List<Exercise> = emptyList(),
    val searchQuery: String = "",
    val selectedMuscleGroup: MuscleGroup = MuscleGroup.ALL,
    val selectedEquipment: Equipment = Equipment.ALL,
    val selectedDifficulty: Difficulty = Difficulty.ALL,
    val selectedType: ExerciseType? = null,
    val showOnlyFavorites: Boolean = false,
    val isLoading: Boolean = false
)

sealed class ExerciseLibraryUiEvent {
    data class OnSearchQueryChanged(val query: String) : ExerciseLibraryUiEvent()
    data class OnMuscleGroupSelected(val muscleGroup: MuscleGroup) : ExerciseLibraryUiEvent()
    data class OnEquipmentSelected(val equipment: Equipment) : ExerciseLibraryUiEvent()
    data class OnDifficultySelected(val difficulty: Difficulty) : ExerciseLibraryUiEvent()
    data class OnTypeSelected(val type: ExerciseType?) : ExerciseLibraryUiEvent()
    data class OnToggleFavorite(val exerciseId: String, val isFavorite: Boolean) : ExerciseLibraryUiEvent()
    object OnToggleFavoritesOnly : ExerciseLibraryUiEvent()
}

@HiltViewModel
class ExerciseLibraryViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository
) : BaseViewModel<ExerciseLibraryUiState, ExerciseLibraryUiEvent>(ExerciseLibraryUiState(isLoading = true)) {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedMuscleGroup = MutableStateFlow(MuscleGroup.ALL)
    private val _selectedEquipment = MutableStateFlow(Equipment.ALL)
    private val _selectedDifficulty = MutableStateFlow(Difficulty.ALL)
    private val _selectedType = MutableStateFlow<ExerciseType?>(null)
    private val _showOnlyFavorites = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<ExerciseLibraryUiState> = combine(
        _searchQuery,
        _selectedMuscleGroup,
        _selectedEquipment,
        _selectedDifficulty,
        _selectedType,
        _showOnlyFavorites
    ) { args: Array<Any?> ->
        FilterParams(
            query = args[0] as String,
            muscle = args[1] as MuscleGroup,
            equip = args[2] as Equipment,
            diff = args[3] as Difficulty,
            type = args[4] as ExerciseType?,
            favs = args[5] as Boolean
        )
    }.flatMapLatest { params ->
        combine(
            exerciseRepository.getFilteredExercises(
                params.query, params.muscle, params.equip, params.diff, params.type, params.favs
            ),
            exerciseRepository.getRecentlyUsedExercises()
        ) { filtered, recent ->
            ExerciseLibraryUiState(
                filteredExercises = filtered,
                recentlyUsed = recent,
                searchQuery = params.query,
                selectedMuscleGroup = params.muscle,
                selectedEquipment = params.equip,
                selectedDifficulty = params.diff,
                selectedType = params.type,
                showOnlyFavorites = params.favs,
                isLoading = false
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExerciseLibraryUiState(isLoading = true)
    )

    private data class FilterParams(
        val query: String,
        val muscle: MuscleGroup,
        val equip: Equipment,
        val diff: Difficulty,
        val type: ExerciseType?,
        val favs: Boolean
    )

    override fun onEvent(event: ExerciseLibraryUiEvent) {
        when (event) {
            is ExerciseLibraryUiEvent.OnSearchQueryChanged -> _searchQuery.value = event.query
            is ExerciseLibraryUiEvent.OnMuscleGroupSelected -> _selectedMuscleGroup.value = event.muscleGroup
            is ExerciseLibraryUiEvent.OnEquipmentSelected -> _selectedEquipment.value = event.equipment
            is ExerciseLibraryUiEvent.OnDifficultySelected -> _selectedDifficulty.value = event.difficulty
            is ExerciseLibraryUiEvent.OnTypeSelected -> _selectedType.value = event.type
            is ExerciseLibraryUiEvent.OnToggleFavorite -> {
                viewModelScope.launch {
                    exerciseRepository.updateFavoriteStatus(event.exerciseId, event.isFavorite)
                }
            }
            ExerciseLibraryUiEvent.OnToggleFavoritesOnly -> _showOnlyFavorites.value = !_showOnlyFavorites.value
        }
    }
}

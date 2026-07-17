package com.example.forgegym.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.forgegym.data.models.*
import com.example.forgegym.data.repository.ProgressRepository
import com.example.forgegym.data.repository.TimeRange
import com.example.forgegym.data.repository.WorkoutFrequencyStats
import com.example.forgegym.data.repository.Achievement
import com.example.forgegym.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class ProgressUiState(
    val weeklyVolumeChart: Map<Long, Double> = emptyMap(),
    val monthlyVolumeChart: Map<Long, Double> = emptyMap(),
    val frequencyStats: WorkoutFrequencyStats = WorkoutFrequencyStats(0, 0, 0, 0.0, 0),
    val muscleDistribution: Map<MuscleGroup, Double> = emptyMap(),
    val heatmapData: Map<Long, Int> = emptyMap(),
    val insights: List<String> = emptyList(),
    val milestones: List<Achievement> = emptyList(),
    val performanceScore: Int = 0,
    val personalRecords: List<PersonalRecord> = emptyList(),
    val selectedExerciseId: String? = null,
    val exerciseProgress: List<ExerciseProgress> = emptyList(),
    val selectedRange: TimeRange = TimeRange.DAYS_30,
    val availableExercises: List<Exercise> = emptyList(),
    val isLoading: Boolean = false
)

sealed class ProgressUiEvent {
    data class OnExerciseSelected(val exerciseId: String?) : ProgressUiEvent()
    data class OnTimeRangeSelected(val range: TimeRange) : ProgressUiEvent()
}

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val progressRepository: ProgressRepository,
    private val workoutRepository: WorkoutRepository
) : BaseViewModel<ProgressUiState, ProgressUiEvent>(ProgressUiState(isLoading = true)) {

    private val _selectedExerciseId = MutableStateFlow<String?>(null)
    private val _selectedRange = MutableStateFlow(TimeRange.DAYS_30)

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<ProgressUiState> = combine(
        progressRepository.getWeeklyVolumeChartData(),
        progressRepository.getMonthlyVolumeChartData(),
        progressRepository.getWorkoutFrequencyStats(),
        progressRepository.getMuscleDistribution(),
        progressRepository.getTrainingHeatmapData(),
        progressRepository.getTrainingInsights(),
        progressRepository.getMilestones(),
        progressRepository.getPerformanceScore(),
        progressRepository.getPersonalRecords(),
        workoutRepository.getAllExercises(),
        _selectedExerciseId,
        _selectedRange
    ) { args: Array<Any?> ->
        AnalyticsParams(
            weeklyVolume = args[0] as Map<Long, Double>,
            monthlyVolume = args[1] as Map<Long, Double>,
            freqStats = args[2] as WorkoutFrequencyStats,
            muscleDist = args[3] as Map<MuscleGroup, Double>,
            heatmap = args[4] as Map<Long, Int>,
            insights = args[5] as List<String>,
            milestones = args[6] as List<Achievement>,
            perfScore = args[7] as Int,
            prs = args[8] as List<PersonalRecord>,
            exercises = args[9] as List<Exercise>,
            selectedExId = args[10] as String?,
            selectedRange = args[11] as TimeRange
        )
    }.flatMapLatest { params ->
        val exerciseFlow = if (params.selectedExId != null) {
            progressRepository.getProgressForExercise(params.selectedExId, params.selectedRange)
        } else {
            flowOf(emptyList())
        }

        exerciseFlow.map { exProgress ->
            ProgressUiState(
                weeklyVolumeChart = params.weeklyVolume,
                monthlyVolumeChart = params.monthlyVolume,
                frequencyStats = params.freqStats,
                muscleDistribution = params.muscleDist,
                heatmapData = params.heatmap,
                insights = params.insights,
                milestones = params.milestones,
                performanceScore = params.perfScore,
                personalRecords = params.prs,
                availableExercises = params.exercises,
                selectedExerciseId = params.selectedExId,
                selectedRange = params.selectedRange,
                exerciseProgress = exProgress,
                isLoading = false
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProgressUiState(isLoading = true)
    )

    private data class AnalyticsParams(
        val weeklyVolume: Map<Long, Double>,
        val monthlyVolume: Map<Long, Double>,
        val freqStats: WorkoutFrequencyStats,
        val muscleDist: Map<MuscleGroup, Double>,
        val heatmap: Map<Long, Int>,
        val insights: List<String>,
        val milestones: List<Achievement>,
        val perfScore: Int,
        val prs: List<PersonalRecord>,
        val exercises: List<Exercise>,
        val selectedExId: String?,
        val selectedRange: TimeRange
    )

    override fun onEvent(event: ProgressUiEvent) {
        when (event) {
            is ProgressUiEvent.OnExerciseSelected -> _selectedExerciseId.value = event.exerciseId
            is ProgressUiEvent.OnTimeRangeSelected -> _selectedRange.value = event.range
        }
    }
}

package com.example.forgegym.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.forgegym.data.local.preferences.PreferencesManager
import com.example.forgegym.data.models.*
import com.example.forgegym.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class HomeUiState(
    val userName: String = "",
    val greeting: String = "",
    val activeSession: WorkoutSession? = null,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastWorkoutDate: Long? = null,
    val weeklyWorkoutsCompleted: Int = 0,
    val weeklyGoal: Int = 4,
    val weeklyVolume: Double = 0.0,
    val weeklySets: Int = 0,
    val weeklyReps: Int = 0,
    val todayWorkout: Workout? = null,
    val allWorkouts: List<Workout> = emptyList(),
    val recentSession: WorkoutSession? = null,
    val prsThisWeek: List<PersonalRecord> = emptyList(),
    val workoutDaysThisWeek: List<Int> = emptyList(),
    val workoutSuggestions: List<ExerciseRecommendation> = emptyList(),
    val trainingLoad: TrainingLoadMetrics? = null,
    val trainingInsights: List<TrainingInsight> = emptyList(),
    val motivationalQuote: String = "",
    val isLoading: Boolean = false
)

sealed class HomeUiEvent {
    data class OnUpdateWeeklyGoal(val goal: Int) : HomeUiEvent()
    data class OnWorkoutClick(val workoutId: String) : HomeUiEvent()
    data class OnSessionClick(val sessionId: String) : HomeUiEvent()
    object OnStartEmptyWorkout : HomeUiEvent()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val historyRepository: HistoryRepository,
    private val sessionRepository: WorkoutSessionRepository,
    private val progressRepository: ProgressRepository,
    private val overloadRepository: ProgressiveOverloadRepository,
    private val profileRepository: ProfileRepository,
    private val preferencesManager: PreferencesManager
) : BaseViewModel<HomeUiState, HomeUiEvent>(HomeUiState(isLoading = true)) {

    private val motivationalQuotes = listOf(
        "Consistency is the key to progress.",
        "Your only limit is you.",
        "The pain you feel today will be the strength you feel tomorrow.",
        "Discipline is doing what needs to be done, even if you don't feel like it.",
        "Success starts with self-discipline.",
        "Action is the foundational key to all success.",
        "Small steps lead to big results."
    )

    override val state: StateFlow<HomeUiState> = combine(
        historyRepository.getSessionHistory(),
        sessionRepository.activeSession,
        progressRepository.getPersonalRecordsThisWeek(),
        preferencesManager.userPreferencesFlow,
        overloadRepository.getTrainingLoadMetrics(),
        overloadRepository.getInsights(),
        overloadRepository.getWorkoutSuggestions(),
        workoutRepository.getAllWorkouts(),
        profileRepository.getUserProfile()
    ) { args: Array<Any?> ->
        val history = args[0] as List<WorkoutSession>
        val activeSession = args[1] as WorkoutSession?
        val prs = args[2] as List<PersonalRecord>
        val preferences = args[3] as UserPreferences
        val load = args[4] as TrainingLoadMetrics
        val insights = args[5] as List<TrainingInsight>
        val suggestions = args[6] as List<ExerciseRecommendation>
        val workouts = args[7] as List<Workout>
        val profile = args[8] as UserProfile

        val now = Calendar.getInstance()
        val startOfWeek = now.apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val completedThisWeek = history.filter { it.endTime != null && it.startTime >= startOfWeek }
        
        val weeklyVolume = completedThisWeek.sumOf { it.totalVolume }
        val weeklySets = completedThisWeek.sumOf { session -> session.exercises.sumOf { it.sets.size } }
        val weeklyReps = completedThisWeek.sumOf { session -> session.exercises.sumOf { it.sets.sumOf { set -> set.reps } } }
        
        val workoutDays = completedThisWeek.map { 
            val cal = Calendar.getInstance().apply { timeInMillis = it.startTime }
            cal.get(Calendar.DAY_OF_WEEK)
        }.distinct()

        val streaks = calculateStreaks(history)

        HomeUiState(
            userName = profile.name.trim().split("\\s+".toRegex()).firstOrNull()?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } ?: "Athlete",
            greeting = getDynamicGreeting(),
            activeSession = activeSession,
            currentStreak = streaks.first,
            longestStreak = streaks.second,
            lastWorkoutDate = history.firstOrNull { it.endTime != null }?.startTime,
            weeklyWorkoutsCompleted = completedThisWeek.size,
            weeklyGoal = preferences.weeklyGoal,
            weeklyVolume = weeklyVolume,
            weeklySets = weeklySets,
            weeklyReps = weeklyReps,
            todayWorkout = workouts.find { it.id == preferences.defaultWorkoutId } ?: workouts.firstOrNull(),
            allWorkouts = workouts,
            recentSession = history.firstOrNull { it.endTime != null },
            prsThisWeek = prs,
            workoutDaysThisWeek = workoutDays,
            workoutSuggestions = suggestions,
            trainingLoad = load,
            trainingInsights = insights,
            motivationalQuote = getDailyQuote(),
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    private fun getDynamicGreeting(): String {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..21 -> "Good Evening"
            else -> "Good Night"
        }
    }

    private fun getDailyQuote(): String {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return motivationalQuotes[dayOfYear % motivationalQuotes.size]
    }

    private fun calculateStreaks(history: List<WorkoutSession>): Pair<Int, Int> {
        val completed = history.filter { it.endTime != null }
            .map { 
                val cal = Calendar.getInstance().apply { timeInMillis = it.startTime }
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }.distinct().sortedDescending()

        if (completed.isEmpty()) return Pair(0, 0)

        var currentStreak = 0
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val yesterday = today - 24 * 60 * 60 * 1000

        if (completed[0] == today || completed[0] == yesterday) {
            currentStreak = 1
            var checkDateStreak = completed[0]
            for (i in 1 until completed.size) {
                if (completed[i] == checkDateStreak - 24 * 60 * 60 * 1000) {
                    currentStreak++
                    checkDateStreak = completed[i]
                } else {
                    break
                }
            }
        }

        var longest = 0
        var tempLongest = 1
        var checkDateLongest = completed[0]
        for (i in 1 until completed.size) {
            if (completed[i] == checkDateLongest - 24 * 60 * 60 * 1000) {
                tempLongest++
                checkDateLongest = completed[i]
            } else {
                longest = maxOf(longest, tempLongest)
                tempLongest = 1
                checkDateLongest = completed[i]
            }
        }
        longest = maxOf(longest, tempLongest)

        return Pair(currentStreak, longest)
    }

    override fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.OnUpdateWeeklyGoal -> {
                viewModelScope.launch {
                    preferencesManager.updateWeeklyGoal(event.goal)
                }
            }
            else -> {}
        }
    }
}

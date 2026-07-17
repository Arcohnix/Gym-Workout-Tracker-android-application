package com.example.forgegym.data.repository

import com.example.forgegym.data.models.*
import kotlinx.coroutines.flow.Flow

/**
 * Manages statistics and progression tracking.
 * Responsible for calculating volume, max weight, and personal records.
 */
interface ProgressRepository {
    fun getProgressForExercise(exerciseId: String, range: TimeRange): Flow<List<ExerciseProgress>>
    fun getPersonalRecords(): Flow<List<PersonalRecord>>
    fun getPersonalRecordForExercise(exerciseId: String): Flow<PersonalRecord?>
    fun getPersonalRecordsThisWeek(): Flow<List<PersonalRecord>>
    
    // Detailed Analytics
    fun getWeeklyVolumeChartData(): Flow<Map<Long, Double>> // Start of week timestamp to volume
    fun getMonthlyVolumeChartData(): Flow<Map<Long, Double>> // Start of month timestamp to volume
    fun getWorkoutFrequencyStats(): Flow<WorkoutFrequencyStats>
    fun getMuscleDistribution(): Flow<Map<MuscleGroup, Double>> // Muscle group to volume
    fun getTrainingHeatmapData(): Flow<Map<Long, Int>> // Timestamp (day) to intensity level
    fun getTrainingInsights(): Flow<List<String>>
    fun getMilestones(): Flow<List<Achievement>>
    fun getPerformanceScore(): Flow<Int>
    
    // Legacy/Core methods
    fun getWeeklyVolume(): Flow<Map<Int, Double>> // Day of week (1-7) to volume
    fun getMonthlyVolume(): Flow<Map<Int, Double>> // Day of month to volume
    fun getWorkoutFrequency(): Flow<Int> // Workouts in the last 7 days
    fun getExerciseFrequency(): Flow<Map<String, Int>> // Exercise Name to count
    fun getAverageWorkoutDuration(): Flow<Int> // In minutes
    fun getWorkoutDurationHistory(): Flow<List<Pair<Long, Int>>> // Timestamp to minutes
    fun getBodyWeightHistory(): Flow<List<BodyWeight>>
    suspend fun logBodyWeight(weight: Double)
}

enum class TimeRange {
    DAYS_7, DAYS_30, DAYS_90, YEAR_1, ALL
}

data class WorkoutFrequencyStats(
    val thisWeek: Int,
    val thisMonth: Int,
    val thisYear: Int,
    val avgPerWeek: Double,
    val consistencyPercentage: Int
)

data class Achievement(
    val title: String,
    val description: String,
    val date: Long?,
    val isUnlocked: Boolean,
    val progress: Float // 0..1
)

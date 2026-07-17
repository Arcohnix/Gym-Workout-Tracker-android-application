package com.example.forgegym.data.models

data class WorkoutSummary(
    val sessionId: String,
    val workoutId: String,
    val workoutName: String,
    val startTime: Long,
    val endTime: Long,
    val durationMinutes: Int,
    val totalVolume: Double,
    val totalSets: Int,
    val totalReps: Int,
    val exercisesCompleted: Int,
    val averageWeight: Double,
    val estimatedCalories: Int,
    val exercises: List<ExerciseSession> = emptyList(),
    val newPersonalRecords: List<PersonalRecord>,
    val comparison: WorkoutComparison? = null
)

data class WorkoutComparison(
    val previousWorkoutName: String,
    val volumeDifference: Double,
    val durationDifferenceMinutes: Int
)

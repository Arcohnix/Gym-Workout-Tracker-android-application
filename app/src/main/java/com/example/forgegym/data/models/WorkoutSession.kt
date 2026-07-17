package com.example.forgegym.data.models

import java.util.UUID

/**
 * Represents an instance of a workout performed by the user.
 */
data class WorkoutSession(
    val id: String = UUID.randomUUID().toString(),
    val workoutId: String,
    val workoutName: String,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val note: String = "",
    val exercises: List<ExerciseSession> = emptyList(),
    val totalVolume: Double = 0.0,
    val newPersonalRecords: List<PersonalRecord> = emptyList(),
    val currentExerciseIndex: Int = 0,
    val isPaused: Boolean = false,
    val lastPausedAt: Long? = null,
    val totalPausedDurationMillis: Long = 0
)

/**
 * Captures the performance of a specific exercise within a session.
 */
data class ExerciseSession(
    val exerciseId: String,
    val exerciseName: String,
    val status: ExerciseStatus = ExerciseStatus.NOT_STARTED,
    val sets: List<CompletedSet> = emptyList()
)

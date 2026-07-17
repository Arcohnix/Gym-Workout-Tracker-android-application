package com.example.forgegym.data.models

import java.util.UUID

/**
 * Represents a workout routine/template (e.g., "Push Day").
 */
data class Workout(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val category: WorkoutCategory,
    val difficulty: Difficulty = Difficulty.INTERMEDIATE,
    val workoutExercises: List<WorkoutExercise> = emptyList(),
    val estimatedDurationMinutes: Int = 0,
    val lastPerformedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

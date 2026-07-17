package com.example.forgegym.data.models

import java.util.UUID

/**
 * Represents an exercise entry within a specific workout routine.
 * Allows customization of rest times and notes per exercise.
 */
data class WorkoutExercise(
    val id: String = UUID.randomUUID().toString(),
    val exercise: Exercise,
    val order: Int,
    val restTimeSeconds: Int = 60,
    val note: String = "",
    val supersetId: String? = null
)

package com.example.forgegym.data.models

/**
 * Represents a milestone achievement for a specific exercise.
 */
data class PersonalRecord(
    val id: String,
    val exerciseId: String,
    val exerciseName: String,
    val weight: Double,
    val reps: Int,
    val volume: Double,
    val estimatedOneRepMax: Double,
    val date: Long,
    val type: PRType
)

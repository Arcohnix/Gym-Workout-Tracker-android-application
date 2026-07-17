package com.example.forgegym.data.models

/**
 * Aggregated data points for tracking progress over time.
 */
data class ExerciseProgress(
    val exerciseId: String,
    val timestamp: Long,
    val maxWeight: Double,
    val totalVolume: Double,
    val oneRepMaxEstimate: Double
)

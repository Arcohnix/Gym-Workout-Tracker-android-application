package com.example.forgegym.data.models

import java.util.UUID

/**
 * Represents a single set performed during a workout session.
 */
data class CompletedSet(
    val id: String = UUID.randomUUID().toString(),
    val weight: Double,
    val reps: Int,
    val note: String = "",
    val isWarmup: Boolean = false,
    val isPersonalRecord: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

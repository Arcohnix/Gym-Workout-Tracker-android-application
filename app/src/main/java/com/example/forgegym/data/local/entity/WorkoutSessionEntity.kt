package com.example.forgegym.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "workout_sessions",
    indices = [
        Index(value = ["startTime"]),
        Index(value = ["workoutId"])
    ]
)
@Serializable
data class WorkoutSessionEntity(
    @PrimaryKey val id: String,
    val workoutId: String,
    val workoutName: String,
    val startTime: Long,
    val endTime: Long?,
    val note: String,
    val totalVolume: Double,
    val currentExerciseIndex: Int = 0,
    val isPaused: Boolean = false,
    val lastPausedAt: Long? = null,
    val totalPausedDurationMillis: Long = 0
)

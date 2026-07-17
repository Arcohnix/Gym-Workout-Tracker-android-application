package com.example.forgegym.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "workout_exercise_cross_ref")
@Serializable
data class WorkoutExerciseCrossRef(
    @PrimaryKey val id: String,
    val workoutId: String,
    val exerciseId: String,
    val order: Int,
    val restTimeSeconds: Int,
    val note: String,
    val supersetId: String? = null
)

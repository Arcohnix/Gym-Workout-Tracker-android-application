package com.example.forgegym.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.forgegym.data.models.Difficulty
import com.example.forgegym.data.models.WorkoutCategory
import kotlinx.serialization.Serializable

@Entity(tableName = "workouts")
@Serializable
data class WorkoutEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val category: WorkoutCategory,
    val difficulty: Difficulty,
    val estimatedDurationMinutes: Int,
    val lastPerformedAt: Long?,
    val createdAt: Long
)

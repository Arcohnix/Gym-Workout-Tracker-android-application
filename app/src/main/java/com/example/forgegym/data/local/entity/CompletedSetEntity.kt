package com.example.forgegym.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "completed_sets")
@Serializable
data class CompletedSetEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val exerciseId: String,
    val weight: Double,
    val reps: Int,
    val note: String,
    val isWarmup: Boolean,
    val isPersonalRecord: Boolean,
    val timestamp: Long
)

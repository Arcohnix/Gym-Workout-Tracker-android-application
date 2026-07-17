package com.example.forgegym.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.forgegym.data.models.PRType
import kotlinx.serialization.Serializable

@Entity(tableName = "personal_records")
@Serializable
data class PersonalRecordEntity(
    @PrimaryKey val id: String,
    val exerciseId: String,
    val exerciseName: String,
    val weight: Double,
    val reps: Int,
    val volume: Double,
    val estimatedOneRepMax: Double,
    val date: Long,
    val type: PRType
)

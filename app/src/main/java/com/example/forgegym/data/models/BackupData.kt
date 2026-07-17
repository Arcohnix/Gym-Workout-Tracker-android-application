package com.example.forgegym.data.models

import com.example.forgegym.data.local.entity.*
import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val version: Int = 1,
    val exportTimestamp: Long = System.currentTimeMillis(),
    val workouts: List<WorkoutEntity> = emptyList(),
    val exercises: List<ExerciseEntity> = emptyList(),
    val crossRefs: List<WorkoutExerciseCrossRef> = emptyList(),
    val sessions: List<WorkoutSessionEntity> = emptyList(),
    val sets: List<CompletedSetEntity> = emptyList(),
    val personalRecords: List<PersonalRecordEntity> = emptyList(),
    val userProfile: UserProfileEntity? = null,
    val bodyMeasurements: List<BodyMeasurementEntity> = emptyList()
)

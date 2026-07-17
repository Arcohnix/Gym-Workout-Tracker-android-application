package com.example.forgegym.data.repository

import com.example.forgegym.data.models.*
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    fun getAllExercises(): Flow<List<Exercise>>
    fun getFilteredExercises(
        query: String,
        muscle: MuscleGroup?,
        equipment: Equipment?,
        difficulty: Difficulty?,
        type: ExerciseType?,
        favoritesOnly: Boolean
    ): Flow<List<Exercise>>
    fun getRecentlyUsedExercises(): Flow<List<Exercise>>
    fun getExerciseById(id: String): Flow<Exercise?>
    suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean)
    fun getExerciseHistoryStats(id: String): Flow<ExerciseHistoryStats>
}

data class ExerciseHistoryStats(
    val lastPerformedAt: Long? = null,
    val personalRecord: PersonalRecord? = null,
    val bestVolume: Double = 0.0,
    val totalSessions: Int = 0,
    val totalSets: Int = 0,
    val averageWeight: Double = 0.0,
    val averageReps: Double = 0.0,
    val estimated1RM: Double = 0.0,
    val weeklyVolumeHistory: Map<Long, Double> = emptyMap(),
    val progression: List<ExerciseProgress> = emptyList()
)

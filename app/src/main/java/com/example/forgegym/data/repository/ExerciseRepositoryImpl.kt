package com.example.forgegym.data.repository

import com.example.forgegym.data.local.dao.ExerciseDao
import com.example.forgegym.data.local.dao.PersonalRecordDao
import com.example.forgegym.data.local.dao.SessionDao
import com.example.forgegym.data.local.toDomain
import com.example.forgegym.data.local.toEntity
import com.example.forgegym.data.models.*
import com.example.forgegym.data.sample.ExerciseSeeder
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

class ExerciseRepositoryImpl @Inject constructor(
    private val exerciseDao: ExerciseDao,
    private val sessionDao: SessionDao,
    private val prDao: PersonalRecordDao
) : ExerciseRepository {

    override fun getAllExercises(): Flow<List<Exercise>> {
        return exerciseDao.getAllExercises()
            .onStart { checkAndSeedDatabase() }
            .map { list -> list.map { it.toDomain() } }
    }

    override fun getFilteredExercises(
        query: String,
        muscle: MuscleGroup?,
        equipment: Equipment?,
        difficulty: Difficulty?,
        type: ExerciseType?,
        favoritesOnly: Boolean
    ): Flow<List<Exercise>> {
        return exerciseDao.getFilteredExercises(
            query = query,
            muscle = if (muscle == MuscleGroup.ALL || muscle == null) null else muscle.name,
            equipment = if (equipment == Equipment.ALL || equipment == null) null else equipment.name,
            difficulty = if (difficulty == Difficulty.ALL || difficulty == null) null else difficulty.name,
            type = if (type == null) null else type.name,
            favoritesOnly = favoritesOnly
        ).map { list -> list.map { it.toDomain() } }
    }

    override fun getRecentlyUsedExercises(): Flow<List<Exercise>> {
        return exerciseDao.getRecentlyUsedExercises().map { list -> list.map { it.toDomain() } }
    }

    override fun getExerciseById(id: String): Flow<Exercise?> {
        return flow {
            emit(exerciseDao.getExerciseById(id)?.toDomain())
        }
    }

    override suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean) {
        exerciseDao.updateFavoriteStatus(id, isFavorite)
    }

    override fun getExerciseHistoryStats(id: String): Flow<ExerciseHistoryStats> {
        return combine(
            sessionDao.getAllSessionsWithSets(),
            prDao.getPersonalRecordsForExercise(id)
        ) { sessions, prs ->
            val exerciseSets = sessions.flatMap { it.sets }.filter { it.exerciseId == id }
            if (exerciseSets.isEmpty()) return@combine ExerciseHistoryStats()

            val lastSession = sessions.filter { it.session.endTime != null && it.sets.any { s -> s.exerciseId == id } }
                .maxByOrNull { it.session.startTime }
            
            val totalSessions = sessions.count { it.sets.any { s -> s.exerciseId == id } }
            val bestVolume = if (exerciseSets.isNotEmpty()) exerciseSets.maxOf { it.weight * it.reps } else 0.0
            val avgWeight = exerciseSets.sumOf { it.weight } / exerciseSets.size
            val avgReps = exerciseSets.sumOf { it.reps }.toDouble() / exerciseSets.size
            val bestPR = prs.maxByOrNull { it.weight }?.toDomain()
            val est1RM = if (bestPR != null) bestPR.weight * (1 + (bestPR.reps / 30.0)) else 0.0

            val progression = sessions.filter { it.session.endTime != null }
                .mapNotNull { sessionWithSets ->
                    val sets = sessionWithSets.sets.filter { it.exerciseId == id }
                    if (sets.isEmpty()) return@mapNotNull null
                    
                    val maxW = sets.maxOf { it.weight }
                    ExerciseProgress(
                        exerciseId = id,
                        timestamp = sessionWithSets.session.startTime,
                        maxWeight = maxW,
                        totalVolume = sets.sumOf { it.weight * it.reps },
                        oneRepMaxEstimate = maxW * (1 + (sets.maxBy { it.weight }.reps / 30.0))
                    )
                }.sortedBy { it.timestamp }

            ExerciseHistoryStats(
                lastPerformedAt = lastSession?.session?.startTime,
                personalRecord = bestPR,
                bestVolume = bestVolume,
                totalSessions = totalSessions,
                totalSets = exerciseSets.size,
                averageWeight = avgWeight,
                averageReps = avgReps,
                estimated1RM = est1RM,
                progression = progression
            )
        }
    }

    private suspend fun checkAndSeedDatabase() {
        if (exerciseDao.getExerciseCount() == 0) {
            val predefined = ExerciseSeeder.getPredefinedExercises()
            predefined.forEach { exerciseDao.insertExercise(it.toEntity()) }
        }
    }
}

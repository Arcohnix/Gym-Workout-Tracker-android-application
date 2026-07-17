package com.example.forgegym.data.repository

import com.example.forgegym.data.local.dao.ExerciseDao
import com.example.forgegym.data.local.dao.SessionDao
import com.example.forgegym.data.local.toDomain
import com.example.forgegym.data.models.WorkoutComparison
import com.example.forgegym.data.models.WorkoutSession
import com.example.forgegym.data.models.WorkoutSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HistoryRepositoryImpl @Inject constructor(
    private val sessionDao: SessionDao,
    private val exerciseDao: ExerciseDao
) : HistoryRepository {

    override fun getSessionHistory(
        searchQuery: String,
        startTime: Long?,
        endTime: Long?
    ): Flow<List<WorkoutSession>> {
        return combine(
            sessionDao.getFilteredSessionsWithSets(searchQuery, startTime, endTime),
            exerciseDao.getAllExercises()
        ) { sessions, exercises ->
            val exerciseNames = exercises.associate { it.id to it.name }
            sessions.map { it.toDomain(exerciseNames) }
        }
    }

    override fun getSessionById(sessionId: String): Flow<WorkoutSession?> {
        return combine(
            sessionDao.getSessionById(sessionId),
            exerciseDao.getAllExercises()
        ) { session, exercises ->
            val exerciseNames = exercises.associate { it.id to it.name }
            session?.toDomain(exerciseNames)
        }
    }

    override fun getWorkoutSummary(sessionId: String): Flow<WorkoutSummary?> {
        return getSessionById(sessionId).map { session ->
            if (session == null) return@map null
            
            val endTime = session.endTime ?: System.currentTimeMillis()
            val totalSets = session.exercises.sumOf { it.sets.size }
            val totalReps = session.exercises.sumOf { it.sets.sumOf { set -> set.reps } }
            val durationMillis = endTime - session.startTime - session.totalPausedDurationMillis
            val durationMinutes = (durationMillis / (1000 * 60)).toInt()
            val avgWeight = if (totalReps > 0) session.totalVolume / totalReps else 0.0
            val calories = (durationMinutes * 5) + (session.totalVolume / 100).toInt()

            // Optimized comparison logic
            val previousSession = sessionDao.getPreviousSession(session.workoutId, session.startTime)

            val comparison = previousSession?.let { prev ->
                val prevVolume = prev.sets.sumOf { it.weight * it.reps }
                val prevDurationMillis = (prev.session.endTime ?: 0) - prev.session.startTime - prev.session.totalPausedDurationMillis
                val prevDurationMinutes = (prevDurationMillis / (1000 * 60)).toInt()

                WorkoutComparison(
                    previousWorkoutName = prev.session.workoutName,
                    volumeDifference = session.totalVolume - prevVolume,
                    durationDifferenceMinutes = durationMinutes - prevDurationMinutes
                )
            }

            WorkoutSummary(
                sessionId = session.id,
                workoutId = session.workoutId,
                workoutName = session.workoutName,
                startTime = session.startTime,
                endTime = endTime,
                durationMinutes = durationMinutes,
                totalVolume = session.totalVolume,
                totalSets = totalSets,
                totalReps = totalReps,
                exercisesCompleted = session.exercises.count { it.sets.isNotEmpty() },
                averageWeight = avgWeight,
                estimatedCalories = calories,
                exercises = session.exercises,
                newPersonalRecords = session.newPersonalRecords,
                comparison = comparison
            )
        }
    }

    override suspend fun deleteSession(sessionId: String) {
        sessionDao.deleteSessionById(sessionId)
        sessionDao.deleteSetsForSession(sessionId)
    }
}

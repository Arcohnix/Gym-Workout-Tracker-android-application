package com.example.forgegym.data.repository

import com.example.forgegym.data.models.WorkoutSession
import com.example.forgegym.data.models.WorkoutSummary
import kotlinx.coroutines.flow.Flow

/**
 * Manages the log of completed workout sessions.
 * Responsible for retrieving past session details and deleting entries.
 */
interface HistoryRepository {
    fun getSessionHistory(
        searchQuery: String = "",
        startTime: Long? = null,
        endTime: Long? = null
    ): Flow<List<WorkoutSession>>

    fun getSessionById(sessionId: String): Flow<WorkoutSession?>
    fun getWorkoutSummary(sessionId: String): Flow<WorkoutSummary?>
    suspend fun deleteSession(sessionId: String)
}

package com.example.forgegym.data.repository

import com.example.forgegym.data.models.CompletedSet
import com.example.forgegym.data.models.WorkoutSession
import kotlinx.coroutines.flow.StateFlow

/**
 * Manages the current live workout session.
 * Responsible for tracking the active timer, sets logged, and persisting the final result.
 */
interface WorkoutSessionRepository {
    val activeSession: StateFlow<WorkoutSession?>
    
    suspend fun startSession(workoutId: String)
    suspend fun logSet(exerciseId: String, weight: Double, reps: Int, note: String, isWarmup: Boolean)
    suspend fun updateSet(setId: String, weight: Double, reps: Int, note: String)
    suspend fun removeSet(setId: String)
    suspend fun updateCurrentExerciseIndex(index: Int)
    suspend fun togglePause()
    suspend fun finishSession(note: String)
    suspend fun discardSession()
    suspend fun getLastPerformance(exerciseId: String): List<CompletedSet>
}

package com.example.forgegym.data.repository

import com.example.forgegym.data.models.Exercise
import com.example.forgegym.data.models.Workout
import kotlinx.coroutines.flow.Flow

/**
 * Manages Workout Routines (templates).
 * Responsible for creating, editing, and retrieving the routines users follow.
 */
interface WorkoutRepository {
    fun getAllWorkouts(): Flow<List<Workout>>
    fun getWorkoutById(id: String): Flow<Workout?>
    suspend fun saveWorkout(workout: Workout)
    suspend fun deleteWorkout(workoutId: String)
    suspend fun duplicateWorkout(workoutId: String)
    fun getAllExercises(): Flow<List<Exercise>>
    fun getFilteredExercises(
        query: String,
        muscle: String?,
        equipment: String?,
        difficulty: String?,
        favoritesOnly: Boolean
    ): Flow<List<Exercise>>
    suspend fun updateExerciseFavoriteStatus(exerciseId: String, isFavorite: Boolean)
}

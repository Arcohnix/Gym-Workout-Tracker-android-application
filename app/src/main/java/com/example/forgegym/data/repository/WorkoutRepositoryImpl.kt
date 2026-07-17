package com.example.forgegym.data.repository

import com.example.forgegym.data.local.dao.ExerciseDao
import com.example.forgegym.data.local.dao.WorkoutDao
import com.example.forgegym.data.local.toDomain
import com.example.forgegym.data.local.toEntity
import com.example.forgegym.data.models.Exercise
import com.example.forgegym.data.models.Workout
import com.example.forgegym.data.sample.ExerciseSeeder
import com.example.forgegym.data.sample.SampleWorkoutData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class WorkoutRepositoryImpl @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val exerciseDao: ExerciseDao
) : WorkoutRepository {

    override fun getAllWorkouts(): Flow<List<Workout>> {
        return workoutDao.getAllWorkoutsWithExercises()
            .onStart { 
                checkAndSeedDatabase()
            }
            .map { list ->
                list.map { it.toDomain() }
            }
    }

    private suspend fun checkAndSeedDatabase() {
        if (exerciseDao.getExerciseCount() == 0) {
            val predefined = ExerciseSeeder.getPredefinedExercises()
            predefined.forEach { exerciseDao.insertExercise(it.toEntity()) }
            
            // Seed initial sample workouts
            SampleWorkoutData.workouts.forEach { saveWorkout(it) }
        }
    }

    override fun getWorkoutById(id: String): Flow<Workout?> {
        return workoutDao.getWorkoutById(id).map { it?.toDomain() }
    }

    override suspend fun saveWorkout(workout: Workout) {
        // Ensure exercise templates exist
        workout.workoutExercises.forEach { workoutExercise ->
            if (exerciseDao.getExerciseById(workoutExercise.exercise.id) == null) {
                exerciseDao.insertExercise(workoutExercise.exercise.toEntity())
            }
        }
        
        workoutDao.saveWorkoutWithExercises(
            workout.toEntity(),
            workout.workoutExercises.map { it.toEntity(workout.id) }
        )
    }

    override suspend fun deleteWorkout(workoutId: String) {
        workoutDao.deleteWorkoutWithExercises(workoutId)
    }

    override suspend fun duplicateWorkout(workoutId: String) {
        val original = workoutDao.getWorkoutById(workoutId).first() ?: return
        val workout = original.toDomain()
        val duplicated = workout.copy(
            id = java.util.UUID.randomUUID().toString(),
            name = "${workout.name} (Copy)",
            createdAt = System.currentTimeMillis(),
            lastPerformedAt = null,
            workoutExercises = workout.workoutExercises.map { it.copy(id = java.util.UUID.randomUUID().toString()) }
        )
        saveWorkout(duplicated)
    }

    override fun getAllExercises(): Flow<List<Exercise>> {
        return exerciseDao.getAllExercises()
            .onStart { checkAndSeedDatabase() }
            .map { list ->
                list.map { it.toDomain() }
            }
    }

    override fun getFilteredExercises(
        query: String,
        muscle: String?,
        equipment: String?,
        difficulty: String?,
        favoritesOnly: Boolean
    ): Flow<List<Exercise>> {
        return exerciseDao.getFilteredExercises(query, muscle, equipment, difficulty, null, favoritesOnly)
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun updateExerciseFavoriteStatus(exerciseId: String, isFavorite: Boolean) {
        exerciseDao.updateFavoriteStatus(exerciseId, isFavorite)
    }
}

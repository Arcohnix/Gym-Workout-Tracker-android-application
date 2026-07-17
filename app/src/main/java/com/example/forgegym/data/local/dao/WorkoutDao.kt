package com.example.forgegym.data.local.dao

import androidx.room.*
import com.example.forgegym.data.local.entity.WorkoutEntity
import com.example.forgegym.data.local.entity.WorkoutExerciseCrossRef
import com.example.forgegym.data.local.entity.WorkoutWithExercises
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Transaction
    @Query("SELECT * FROM workouts")
    fun getAllWorkoutsWithExercises(): Flow<List<WorkoutWithExercises>>

    @Transaction
    @Query("SELECT * FROM workouts WHERE id = :id")
    fun getWorkoutById(id: String): Flow<WorkoutWithExercises?>

    @Query("SELECT * FROM workouts")
    fun getAllWorkouts(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workout_exercise_cross_ref")
    fun getAllCrossRefs(): Flow<List<WorkoutExerciseCrossRef>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity)

    @Query("DELETE FROM workouts WHERE id = :workoutId")
    suspend fun deleteWorkoutById(workoutId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutExerciseCrossRef(crossRef: WorkoutExerciseCrossRef)

    @Query("DELETE FROM workout_exercise_cross_ref WHERE workoutId = :workoutId")
    suspend fun deleteCrossRefsForWorkout(workoutId: String)
    @Transaction
    suspend fun saveWorkoutWithExercises(
        workout: WorkoutEntity,
        crossRefs: List<WorkoutExerciseCrossRef>
    ) {
        insertWorkout(workout)
        deleteCrossRefsForWorkout(workout.id)
        crossRefs.forEach { insertWorkoutExerciseCrossRef(it) }
    }

    @Transaction
    suspend fun deleteWorkoutWithExercises(workoutId: String) {
        deleteWorkoutById(workoutId)
        deleteCrossRefsForWorkout(workoutId)
    }
}

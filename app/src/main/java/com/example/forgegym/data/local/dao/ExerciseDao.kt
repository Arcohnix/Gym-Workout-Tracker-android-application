package com.example.forgegym.data.local.dao

import androidx.room.*
import com.example.forgegym.data.local.entity.ExerciseEntity
import com.example.forgegym.data.models.Difficulty
import com.example.forgegym.data.models.Equipment
import com.example.forgegym.data.models.ExerciseType
import com.example.forgegym.data.models.MuscleGroup
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises ORDER BY name ASC")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    @Query("""
        SELECT * FROM exercises 
        WHERE name LIKE '%' || :query || '%'
        AND (:muscle IS NULL OR primaryMuscleGroup = :muscle)
        AND (:equipment IS NULL OR equipment = :equipment)
        AND (:difficulty IS NULL OR difficulty = :difficulty)
        AND (:type IS NULL OR exerciseType = :type)
        AND (:favoritesOnly = 0 OR isFavorite = 1)
        ORDER BY name ASC
    """)
    fun getFilteredExercises(
        query: String,
        muscle: String?,
        equipment: String?,
        difficulty: String?,
        type: String?,
        favoritesOnly: Boolean
    ): Flow<List<ExerciseEntity>>

    @Query("""
        SELECT e.* FROM exercises e
        JOIN completed_sets s ON e.id = s.exerciseId
        GROUP BY e.id
        ORDER BY MAX(s.timestamp) DESC
        LIMIT 10
    """)
    fun getRecentlyUsedExercises(): Flow<List<ExerciseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseEntity)

    @Query("UPDATE exercises SET isFavorite = :isFavorite WHERE id = :exerciseId")
    suspend fun updateFavoriteStatus(exerciseId: String, isFavorite: Boolean)

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getExerciseById(id: String): ExerciseEntity?

    @Query("SELECT count(*) FROM exercises")
    suspend fun getExerciseCount(): Int
}

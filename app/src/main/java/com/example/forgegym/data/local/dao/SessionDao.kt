package com.example.forgegym.data.local.dao

import androidx.room.*
import com.example.forgegym.data.local.entity.CompletedSetEntity
import com.example.forgegym.data.local.entity.SessionWithSets
import com.example.forgegym.data.local.entity.WorkoutSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Transaction
    @Query("SELECT * FROM workout_sessions ORDER BY startTime DESC")
    fun getAllSessionsWithSets(): Flow<List<SessionWithSets>>

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId")
    fun getSessionById(sessionId: String): Flow<SessionWithSets?>

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE endTime IS NULL LIMIT 1")
    fun getActiveSession(): Flow<SessionWithSets?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: CompletedSetEntity)

    @Query("DELETE FROM completed_sets WHERE id = :setId")
    suspend fun deleteSetById(setId: String)

    @Query("DELETE FROM workout_sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: String)

    @Query("DELETE FROM completed_sets WHERE sessionId = :sessionId")
    suspend fun deleteSetsForSession(sessionId: String)

    @Query("SELECT * FROM workout_sessions")
    fun getAllSessions(): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM completed_sets")
    fun getAllSets(): Flow<List<CompletedSetEntity>>
    @Transaction
    @Query("""
        SELECT * FROM workout_sessions 
        WHERE endTime IS NOT NULL 
        AND workoutName LIKE '%' || :query || '%'
        AND (:startTime IS NULL OR startTime >= :startTime)
        AND (:endTime IS NULL OR startTime <= :endTime)
        ORDER BY startTime DESC
    """)
    fun getFilteredSessionsWithSets(
        query: String,
        startTime: Long?,
        endTime: Long?
    ): Flow<List<SessionWithSets>>
    @Transaction
    @Query("""
        SELECT * FROM workout_sessions 
        WHERE workoutId = :workoutId 
        AND endTime IS NOT NULL 
        AND startTime < :beforeTime 
        ORDER BY startTime DESC 
        LIMIT 1
    """)
    suspend fun getPreviousSession(workoutId: String, beforeTime: Long): SessionWithSets?
}

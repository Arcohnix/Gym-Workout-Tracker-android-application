package com.example.forgegym.data.local.dao

import androidx.room.*
import com.example.forgegym.data.local.entity.PersonalRecordEntity
import com.example.forgegym.data.models.PRType
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonalRecordDao {
    @Query("SELECT * FROM personal_records ORDER BY date DESC")
    fun getAllPersonalRecords(): Flow<List<PersonalRecordEntity>>

    @Query("SELECT * FROM personal_records WHERE exerciseId = :exerciseId")
    fun getPersonalRecordsForExercise(exerciseId: String): Flow<List<PersonalRecordEntity>>

    @Query("SELECT * FROM personal_records WHERE exerciseId = :exerciseId AND type = :type ORDER BY weight DESC, reps DESC, date DESC LIMIT 1")
    suspend fun getBestRecord(exerciseId: String, type: PRType): PersonalRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPersonalRecord(personalRecord: PersonalRecordEntity)
}

package com.example.forgegym.data.repository

import androidx.room.withTransaction
import com.example.forgegym.data.local.dao.*
import com.example.forgegym.data.local.database.AppDatabase
import com.example.forgegym.data.local.preferences.PreferencesManager
import com.example.forgegym.data.models.BackupData
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class BackupRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val workoutDao: WorkoutDao,
    private val exerciseDao: ExerciseDao,
    private val sessionDao: SessionDao,
    private val prDao: PersonalRecordDao,
    private val userProfileDao: UserProfileDao,
    private val bodyMeasurementDao: BodyMeasurementDao,
    private val bodyWeightDao: BodyWeightDao,
    private val preferencesManager: PreferencesManager
) : BackupRepository {

    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    override suspend fun exportBackup(): String {
        val backupData = BackupData(
            workouts = workoutDao.getAllWorkouts().first(),
            exercises = exerciseDao.getAllExercises().first(),
            crossRefs = workoutDao.getAllCrossRefs().first(),
            sessions = sessionDao.getAllSessions().first(),
            sets = sessionDao.getAllSets().first(),
            personalRecords = prDao.getAllPersonalRecords().first(),
            userProfile = userProfileDao.getUserProfile().first(),
            bodyMeasurements = bodyMeasurementDao.getAllMeasurements().first()
        )
        return json.encodeToString(backupData)
    }

    override suspend fun importBackup(jsonString: String): Result<Unit> {
        return try {
            val backupData = json.decodeFromString<BackupData>(jsonString)
            
            if (backupData.version < 1) return Result.failure(Exception("Invalid backup version"))

            database.withTransaction {
                backupData.exercises.forEach { exerciseDao.insertExercise(it) }
                backupData.workouts.forEach { workoutDao.insertWorkout(it) }
                backupData.crossRefs.forEach { workoutDao.insertWorkoutExerciseCrossRef(it) }
                backupData.sessions.forEach { sessionDao.insertSession(it) }
                backupData.sets.forEach { sessionDao.insertSet(it) }
                backupData.personalRecords.forEach { prDao.insertPersonalRecord(it) }
                backupData.userProfile?.let { userProfileDao.insertProfile(it) }
                backupData.bodyMeasurements.forEach { bodyMeasurementDao.insertMeasurement(it) }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun exportHistoryToCsv(): String {
        val sessions = sessionDao.getAllSessions().first()
        val sets = sessionDao.getAllSets().first()
        val exercises = exerciseDao.getAllExercises().first().associateBy { it.id }

        val sb = StringBuilder()
        sb.append("Date,Workout,Exercise,Weight,Reps,Note,Volume\n")

        sessions.filter { it.endTime != null }.sortedByDescending { it.startTime }.forEach { session ->
            val sessionSets = sets.filter { it.sessionId == session.id }
            sessionSets.forEach { set ->
                val exerciseName = exercises[set.exerciseId]?.name ?: "Unknown"
                val date = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(set.timestamp))
                sb.append("\"$date\",\"${session.workoutName}\",\"$exerciseName\",${set.weight},${set.reps},\"${set.note}\",${set.weight * set.reps}\n")
            }
        }
        return sb.toString()
    }

    override suspend fun deleteAllData() {
        database.withTransaction {
            database.clearAllTables()
        }
    }

    override suspend fun resetApp() {
        deleteAllData()
        preferencesManager.clearAll()
    }
}

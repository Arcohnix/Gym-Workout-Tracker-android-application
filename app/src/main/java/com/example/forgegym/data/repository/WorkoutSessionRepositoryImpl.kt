package com.example.forgegym.data.repository

import com.example.forgegym.data.local.dao.ExerciseDao
import com.example.forgegym.data.local.dao.PersonalRecordDao
import com.example.forgegym.data.local.dao.SessionDao
import com.example.forgegym.data.local.dao.WorkoutDao
import com.example.forgegym.data.local.entity.PersonalRecordEntity
import com.example.forgegym.data.local.entity.WorkoutSessionEntity
import com.example.forgegym.data.local.toDomain
import com.example.forgegym.data.local.toEntity
import com.example.forgegym.data.models.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

class WorkoutSessionRepositoryImpl @Inject constructor(
    private val sessionDao: SessionDao,
    private val workoutDao: WorkoutDao,
    private val exerciseDao: ExerciseDao,
    private val prDao: PersonalRecordDao
) : WorkoutSessionRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _activeSession = MutableStateFlow<WorkoutSession?>(null)
    override val activeSession: StateFlow<WorkoutSession?> = _activeSession.asStateFlow()

    private val _newPRsAchieved = MutableStateFlow<List<PersonalRecord>>(emptyList())

    init {
        @OptIn(ExperimentalCoroutinesApi::class)
        sessionDao.getActiveSession().flatMapLatest { sessionWithSets ->
            if (sessionWithSets == null) {
                flowOf(null)
            } else {
                // Fetch exercise names to ensure toDomain mapping works correctly
                combine(
                    workoutDao.getWorkoutById(sessionWithSets.session.workoutId),
                    exerciseDao.getAllExercises(),
                    _newPRsAchieved
                ) { workoutWithExercises, allExercises, newPRs ->
                    val exerciseNames = allExercises.associate { it.id to it.name }
                    val baseSession = sessionWithSets.toDomain(exerciseNames)
                    
                    val sessionExercises = if (workoutWithExercises != null) {
                        workoutWithExercises.exercises.sortedBy { it.crossRef.order }.map { we ->
                            val existingExerciseSession = baseSession.exercises.find { it.exerciseId == we.exercise.id }
                            existingExerciseSession ?: ExerciseSession(
                                exerciseId = we.exercise.id,
                                exerciseName = we.exercise.name,
                                sets = emptyList()
                            )
                        }
                    } else {
                        baseSession.exercises
                    }

                    baseSession.copy(
                        exercises = sessionExercises,
                        newPersonalRecords = newPRs
                    )
                }
            }
        }.onEach { session ->
            _activeSession.value = session
        }.launchIn(repositoryScope)
    }

    override suspend fun startSession(workoutId: String) {
        val currentActive = sessionDao.getActiveSession().firstOrNull()
        if (currentActive != null && currentActive.session.workoutId == workoutId) {
            return
        }

        _newPRsAchieved.value = emptyList()
        val workoutWithExercises = workoutDao.getWorkoutById(workoutId).firstOrNull() ?: return
        val workout = workoutWithExercises.workout
        
        val session = WorkoutSessionEntity(
            id = UUID.randomUUID().toString(),
            workoutId = workoutId,
            workoutName = workout.name,
            startTime = System.currentTimeMillis(),
            endTime = null,
            note = "",
            totalVolume = 0.0,
            currentExerciseIndex = 0,
            isPaused = false
        )
        sessionDao.insertSession(session)
    }

    override suspend fun logSet(exerciseId: String, weight: Double, reps: Int, note: String, isWarmup: Boolean) {
        val currentSessionWithSets = sessionDao.getActiveSession().firstOrNull() ?: return
        val currentSession = currentSessionWithSets.session

        val set = CompletedSet(
            weight = weight,
            reps = reps,
            note = note,
            isWarmup = isWarmup
        )
        
        val isPR = checkForNewPRs(exerciseId, weight, reps)
        
        sessionDao.insertSet(set.copy(isPersonalRecord = isPR).toEntity(currentSession.id, exerciseId))
        
        val updatedVolume = currentSession.totalVolume + (weight * reps)
        sessionDao.insertSession(currentSession.copy(totalVolume = updatedVolume))
    }

    override suspend fun updateCurrentExerciseIndex(index: Int) {
        val currentSession = sessionDao.getActiveSession().firstOrNull()?.session ?: return
        sessionDao.insertSession(currentSession.copy(currentExerciseIndex = index))
    }

    override suspend fun togglePause() {
        val currentSession = sessionDao.getActiveSession().firstOrNull()?.session ?: return
        val now = System.currentTimeMillis()
        
        val updatedSession = if (currentSession.isPaused) {
            val pauseDuration = now - (currentSession.lastPausedAt ?: now)
            currentSession.copy(
                isPaused = false,
                lastPausedAt = null,
                totalPausedDurationMillis = currentSession.totalPausedDurationMillis + pauseDuration
            )
        } else {
            currentSession.copy(
                isPaused = true,
                lastPausedAt = now
            )
        }
        sessionDao.insertSession(updatedSession)
    }

    private suspend fun checkForNewPRs(exerciseId: String, weight: Double, reps: Int): Boolean {
        val volume = weight * reps
        val e1rm = weight * (1 + (reps / 30.0))
        var isAnyPR = false
        val exercise = exerciseDao.getExerciseById(exerciseId) ?: return false

        val bestWeight = prDao.getBestRecord(exerciseId, PRType.MAX_WEIGHT)
        if (bestWeight == null || weight > bestWeight.weight) {
            savePR(exerciseId, exercise.name, weight, reps, volume, e1rm, PRType.MAX_WEIGHT)
            isAnyPR = true
        }

        val bestReps = prDao.getBestRecord(exerciseId, PRType.MAX_REPS)
        if (bestReps == null || reps > bestReps.reps) {
            savePR(exerciseId, exercise.name, weight, reps, volume, e1rm, PRType.MAX_REPS)
            isAnyPR = true
        }

        val bestVolume = prDao.getBestRecord(exerciseId, PRType.MAX_VOLUME)
        if (bestVolume == null || volume > bestVolume.volume) {
            savePR(exerciseId, exercise.name, weight, reps, volume, e1rm, PRType.MAX_VOLUME)
            isAnyPR = true
        }

        val bestE1RM = prDao.getBestRecord(exerciseId, PRType.ESTIMATED_1RM)
        if (bestE1RM == null || e1rm > bestE1RM.estimatedOneRepMax) {
            savePR(exerciseId, exercise.name, weight, reps, volume, e1rm, PRType.ESTIMATED_1RM)
            isAnyPR = true
        }

        return isAnyPR
    }

    private suspend fun savePR(exId: String, name: String, w: Double, r: Int, v: Double, e1rm: Double, type: PRType) {
        val pr = PersonalRecord(
            id = UUID.randomUUID().toString(),
            exerciseId = exId,
            exerciseName = name,
            weight = w,
            reps = r,
            volume = v,
            estimatedOneRepMax = e1rm,
            date = System.currentTimeMillis(),
            type = type
        )
        prDao.insertPersonalRecord(pr.toEntity())
        _newPRsAchieved.update { it + pr }
    }

    override suspend fun updateSet(setId: String, weight: Double, reps: Int, note: String) {
        // Implementation for set update
    }

    override suspend fun removeSet(setId: String) {
        sessionDao.deleteSetById(setId)
    }

    override suspend fun finishSession(note: String) {
        val currentSessionWithSets = sessionDao.getActiveSession().first() ?: return
        val currentSession = currentSessionWithSets.session
        
        val updatedSession = currentSession.copy(
            endTime = System.currentTimeMillis(),
            note = note
        )
        sessionDao.insertSession(updatedSession)
        
        workoutDao.getWorkoutById(currentSession.workoutId).first()?.let { workoutWithExercises ->
            workoutDao.insertWorkout(
                workoutWithExercises.workout.copy(lastPerformedAt = System.currentTimeMillis())
            )
        }
        _newPRsAchieved.value = emptyList()
    }

    override suspend fun discardSession() {
        val currentSession = sessionDao.getActiveSession().firstOrNull()?.session ?: return
        sessionDao.deleteSessionById(currentSession.id)
        sessionDao.deleteSetsForSession(currentSession.id)
        _newPRsAchieved.value = emptyList()
    }

    override suspend fun getLastPerformance(exerciseId: String): List<CompletedSet> {
        val lastSessionWithSets = sessionDao.getFilteredSessionsWithSets("", null, null).first()
            .find { it.sets.any { set -> set.exerciseId == exerciseId } }
            
        return lastSessionWithSets?.sets
            ?.filter { it.exerciseId == exerciseId }
            ?.map { it.toDomain() } 
            ?: emptyList()
    }
}

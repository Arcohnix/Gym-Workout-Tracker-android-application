package com.example.forgegym.data.repository

import com.example.forgegym.data.local.dao.BodyWeightDao
import com.example.forgegym.data.local.dao.PersonalRecordDao
import com.example.forgegym.data.local.dao.SessionDao
import com.example.forgegym.data.local.dao.ExerciseDao
import com.example.forgegym.data.local.entity.BodyWeightEntity
import com.example.forgegym.data.local.toDomain
import com.example.forgegym.data.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

class ProgressRepositoryImpl @Inject constructor(
    private val sessionDao: SessionDao,
    private val bodyWeightDao: BodyWeightDao,
    private val prDao: PersonalRecordDao,
    private val exerciseDao: ExerciseDao
) : ProgressRepository {

    override fun getProgressForExercise(exerciseId: String, range: TimeRange): Flow<List<ExerciseProgress>> {
        return sessionDao.getAllSessionsWithSets().map { sessions ->
            val now = System.currentTimeMillis()
            val cutoff = when (range) {
                TimeRange.DAYS_7 -> now - (7L * 24 * 60 * 60 * 1000)
                TimeRange.DAYS_30 -> now - (30L * 24 * 60 * 60 * 1000)
                TimeRange.DAYS_90 -> now - (90L * 24 * 60 * 60 * 1000)
                TimeRange.YEAR_1 -> now - (365L * 24 * 60 * 60 * 1000)
                TimeRange.ALL -> 0L
            }

            sessions
                .filter { it.session.startTime >= cutoff && it.session.endTime != null }
                .mapNotNull { sessionWithSets ->
                    val exerciseSets = sessionWithSets.sets.filter { it.exerciseId == exerciseId }
                    if (exerciseSets.isEmpty()) return@mapNotNull null
                    
                    val maxWeight = exerciseSets.maxOf { it.weight }
                    val volume = exerciseSets.sumOf { it.weight * it.reps }
                    
                    ExerciseProgress(
                        exerciseId = exerciseId,
                        timestamp = sessionWithSets.session.startTime,
                        maxWeight = maxWeight,
                        totalVolume = volume,
                        oneRepMaxEstimate = maxWeight * (1 + (exerciseSets.maxBy { it.weight }.reps / 30.0))
                    )
                }.sortedBy { it.timestamp }
        }
    }

    override fun getPersonalRecords(): Flow<List<PersonalRecord>> {
        return prDao.getAllPersonalRecords().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getPersonalRecordForExercise(exerciseId: String): Flow<PersonalRecord?> {
        return prDao.getPersonalRecordsForExercise(exerciseId).map { list ->
            list.maxByOrNull { it.weight }?.toDomain()
        }
    }

    override fun getPersonalRecordsThisWeek(): Flow<List<PersonalRecord>> {
        val weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
        return prDao.getAllPersonalRecords().map { entities ->
            entities.filter { it.date >= weekAgo }.map { it.toDomain() }
        }
    }

    override fun getWeeklyVolumeChartData(): Flow<Map<Long, Double>> {
        return sessionDao.getAllSessionsWithSets().map { sessions ->
            val calendar = Calendar.getInstance()
            val volumeByWeek = mutableMapOf<Long, Double>()
            
            for (i in 0 until 12) {
                val cal = Calendar.getInstance()
                cal.add(Calendar.WEEK_OF_YEAR, -i)
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                volumeByWeek[cal.timeInMillis] = 0.0
            }

            sessions.filter { it.session.endTime != null }.forEach { sessionWithSets ->
                calendar.timeInMillis = sessionWithSets.session.startTime
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                
                val weekStart = calendar.timeInMillis
                if (volumeByWeek.containsKey(weekStart)) {
                    val volume = sessionWithSets.sets.sumOf { it.weight * it.reps }
                    volumeByWeek[weekStart] = (volumeByWeek[weekStart] ?: 0.0) + volume
                }
            }
            volumeByWeek.toSortedMap()
        }
    }

    override fun getMonthlyVolumeChartData(): Flow<Map<Long, Double>> {
        return sessionDao.getAllSessionsWithSets().map { sessions ->
            val calendar = Calendar.getInstance()
            val volumeByMonth = mutableMapOf<Long, Double>()
            
            for (i in 0 until 12) {
                val cal = Calendar.getInstance()
                cal.add(Calendar.MONTH, -i)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                volumeByMonth[cal.timeInMillis] = 0.0
            }

            sessions.filter { it.session.endTime != null }.forEach { sessionWithSets ->
                calendar.timeInMillis = sessionWithSets.session.startTime
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                
                val monthStart = calendar.timeInMillis
                if (volumeByMonth.containsKey(monthStart)) {
                    val volume = sessionWithSets.sets.sumOf { it.weight * it.reps }
                    volumeByMonth[monthStart] = (volumeByMonth[monthStart] ?: 0.0) + volume
                }
            }
            volumeByMonth.toSortedMap()
        }
    }

    override fun getWorkoutFrequencyStats(): Flow<WorkoutFrequencyStats> {
        return sessionDao.getAllSessionsWithSets().map { sessions ->
            val now = Calendar.getInstance()
            
            val startOfWeek = now.apply {
                set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                set(Calendar.HOUR_OF_DAY, 0)
            }.timeInMillis
            
            val startOfMonth = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
            }.timeInMillis
            
            val startOfYear = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
            }.timeInMillis

            val completed = sessions.filter { it.session.endTime != null }
            
            val thisWeek = completed.count { it.session.startTime >= startOfWeek }
            val thisMonth = completed.count { it.session.startTime >= startOfMonth }
            val thisYear = completed.count { it.session.startTime >= startOfYear }
            
            val threeMonthsAgo = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000)
            val recentCompleted = completed.filter { it.session.startTime >= threeMonthsAgo }
            val avg = if (recentCompleted.isNotEmpty()) recentCompleted.size / 12.0 else 0.0

            WorkoutFrequencyStats(
                thisWeek = thisWeek,
                thisMonth = thisMonth,
                thisYear = thisYear,
                avgPerWeek = avg,
                consistencyPercentage = (avg / 4.0 * 100).toInt().coerceIn(0, 100)
            )
        }
    }

    override fun getMuscleDistribution(): Flow<Map<MuscleGroup, Double>> {
        return combine(
            sessionDao.getAllSessionsWithSets(),
            exerciseDao.getAllExercises()
        ) { sessions, exercises ->
            val exerciseMuscles = exercises.associate { it.id to it.primaryMuscleGroup }
            val distribution = mutableMapOf<MuscleGroup, Double>()
            
            sessions.filter { it.session.endTime != null }.flatMap { it.sets }.forEach { set ->
                val muscle = exerciseMuscles[set.exerciseId] ?: MuscleGroup.OTHER
                val volume = set.weight * set.reps
                distribution[muscle] = (distribution[muscle] ?: 0.0) + volume
            }
            distribution
        }
    }

    override fun getTrainingHeatmapData(): Flow<Map<Long, Int>> {
        return sessionDao.getAllSessionsWithSets().map { sessions ->
            val dailyVolume = mutableMapOf<Long, Double>()
            sessions.filter { it.session.endTime != null }.forEach { sessionWithSets ->
                val cal = Calendar.getInstance().apply { 
                    timeInMillis = sessionWithSets.session.startTime
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val day = cal.timeInMillis
                val volume = sessionWithSets.sets.sumOf { it.weight * it.reps }
                dailyVolume[day] = (dailyVolume[day] ?: 0.0) + volume
            }
            
            val maxDailyVolume = dailyVolume.values.maxOrNull() ?: 1.0
            dailyVolume.mapValues { (_, volume) ->
                when {
                    volume >= maxDailyVolume * 0.8 -> 4
                    volume >= maxDailyVolume * 0.5 -> 3
                    volume >= maxDailyVolume * 0.2 -> 2
                    else -> 1
                }
            }
        }
    }

    override fun getTrainingInsights(): Flow<List<String>> {
        return combine(
            sessionDao.getAllSessionsWithSets(),
            exerciseDao.getAllExercises()
        ) { sessions, exercises ->
            val insights = mutableListOf<String>()
            val completed = sessions.filter { it.session.endTime != null }.sortedByDescending { it.session.startTime }
            if (completed.isEmpty()) return@combine emptyList()

            val muscleGroups = MuscleGroup.entries.filter { it != MuscleGroup.ALL && it != MuscleGroup.OTHER }
            val exerciseMuscles = exercises.associate { it.id to it.primaryMuscleGroup }
            
            for (muscle in muscleGroups) {
                val lastTrained = completed.find { session -> 
                    session.sets.any { set -> exerciseMuscles[set.exerciseId] == muscle }
                }
                if (lastTrained == null) {
                    insights.add("You haven't trained ${muscle.name.lowercase()} yet.")
                } else {
                    val days = (System.currentTimeMillis() - lastTrained.session.startTime) / (1000 * 60 * 60 * 24)
                    if (days > 7) {
                        insights.add("It's been $days days since you last trained ${muscle.name.lowercase()}.")
                    }
                }
            }

            if (completed.size >= 5) {
                insights.add("You've completed ${completed.size} workouts! Keep the momentum going.")
            }

            insights.take(5)
        }
    }

    override fun getMilestones(): Flow<List<Achievement>> {
        return sessionDao.getAllSessionsWithSets().map { sessions ->
            val completed = sessions.filter { it.session.endTime != null }
            val totalSets = completed.sumOf { it.sets.size }
            val totalVolume = completed.sumOf { it.sets.sumOf { set -> set.weight * set.reps } }
            
            listOf(
                createAchievement("Centurion", "Complete 100 Workouts", null, completed.size >= 100, completed.size / 100f),
                createAchievement("Set Master", "Log 1,000 Sets", null, totalSets >= 1000, totalSets / 1000f),
                createAchievement("Heavy Lifter", "Lift 100,000 kg total", null, totalVolume >= 100000, (totalVolume / 100000f).toFloat()),
                createAchievement("First Step", "Complete your first workout", completed.firstOrNull()?.session?.startTime, completed.isNotEmpty(), 1f)
            )
        }
    }
    
    private fun createAchievement(title: String, desc: String, date: Long?, unlocked: Boolean, progress: Float) = 
        Achievement(title, desc, date, unlocked, progress.coerceIn(0f, 1f))

    override fun getPerformanceScore(): Flow<Int> {
        return combine(
            getWorkoutFrequencyStats(),
            sessionDao.getAllSessionsWithSets()
        ) { stats, _ ->
            val consistency = stats.consistencyPercentage * 0.4
            val frequency = (stats.avgPerWeek / 5.0 * 100).coerceAtMost(100.0) * 0.3
            val volume = 20.0 
            val completion = 10.0 
            
            (consistency + frequency + volume + completion).toInt().coerceIn(0, 100)
        }
    }

    override fun getWeeklyVolume(): Flow<Map<Int, Double>> {
        return sessionDao.getAllSessionsWithSets().map { sessions ->
            val calendar = Calendar.getInstance()
            val now = System.currentTimeMillis()
            val sevenDaysAgo = now - (7 * 24 * 60 * 60 * 1000)
            
            val lastSevenDaysSessions = sessions.filter { it.session.startTime >= sevenDaysAgo }
            
            val volumeByDay = mutableMapOf<Int, Double>()
            for (i in 0..6) {
                calendar.timeInMillis = now - (i * 24 * 60 * 60 * 1000)
                volumeByDay[calendar.get(Calendar.DAY_OF_WEEK)] = 0.0
            }

            lastSevenDaysSessions.forEach { sessionWithSets ->
                calendar.timeInMillis = sessionWithSets.session.startTime
                val day = calendar.get(Calendar.DAY_OF_WEEK)
                val volume = sessionWithSets.sets.sumOf { it.weight * it.reps }
                volumeByDay[day] = (volumeByDay[day] ?: 0.0) + volume
            }
            volumeByDay
        }
    }

    override fun getMonthlyVolume(): Flow<Map<Int, Double>> {
        return sessionDao.getAllSessionsWithSets().map { sessions ->
            val calendar = Calendar.getInstance()
            val now = System.currentTimeMillis()
            val thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000)
            
            val lastMonthSessions = sessions.filter { it.session.startTime >= thirtyDaysAgo }
            
            val volumeByDay = mutableMapOf<Int, Double>()
            lastMonthSessions.forEach { sessionWithSets ->
                calendar.timeInMillis = sessionWithSets.session.startTime
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                val volume = sessionWithSets.sets.sumOf { it.weight * it.reps }
                volumeByDay[day] = (volumeByDay[day] ?: 0.0) + volume
            }
            volumeByDay
        }
    }

    override fun getWorkoutFrequency(): Flow<Int> {
        return sessionDao.getAllSessionsWithSets().map { sessions ->
            val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
            sessions.count { it.session.startTime >= sevenDaysAgo && it.session.endTime != null }
        }
    }

    override fun getExerciseFrequency(): Flow<Map<String, Int>> {
        return combine(
            sessionDao.getAllSessionsWithSets(),
            exerciseDao.getAllExercises()
        ) { sessions, exercises ->
            val exerciseNames = exercises.associate { it.id to it.name }
            val counts = mutableMapOf<String, Int>()
            
            sessions.flatMap { it.sets }.forEach { set ->
                val name = exerciseNames[set.exerciseId] ?: "Unknown"
                counts[name] = (counts[name] ?: 0) + 1
            }
            counts.toList().sortedByDescending { it.second }.take(5).toMap()
        }
    }

    override fun getAverageWorkoutDuration(): Flow<Int> {
        return sessionDao.getAllSessionsWithSets().map { sessions ->
            val completedSessions = sessions.filter { it.session.endTime != null }
            if (completedSessions.isEmpty()) return@map 0
            
            val totalDuration = completedSessions.sumOf { 
                (it.session.endTime!! - it.session.startTime - it.session.totalPausedDurationMillis) / (1000 * 60)
            }
            (totalDuration / completedSessions.size).toInt()
        }
    }

    override fun getWorkoutDurationHistory(): Flow<List<Pair<Long, Int>>> {
        return sessionDao.getAllSessionsWithSets().map { sessions ->
            sessions
                .filter { it.session.endTime != null }
                .map { 
                    val duration = ((it.session.endTime!! - it.session.startTime - it.session.totalPausedDurationMillis) / (1000 * 60)).toInt()
                    Pair(it.session.startTime, duration)
                }
                .sortedBy { it.first }
        }
    }

    override fun getBodyWeightHistory(): Flow<List<BodyWeight>> {
        return bodyWeightDao.getAllBodyWeights().map { entities ->
            entities.map { BodyWeight(it.id, it.weight, it.date) }
        }
    }

    override suspend fun logBodyWeight(weight: Double) {
        bodyWeightDao.insertBodyWeight(
            BodyWeightEntity(
                id = UUID.randomUUID().toString(),
                weight = weight,
                date = System.currentTimeMillis()
            )
        )
    }
}

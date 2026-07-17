package com.example.forgegym.data.repository

import com.example.forgegym.data.local.dao.ExerciseDao
import com.example.forgegym.data.local.dao.SessionDao
import com.example.forgegym.data.local.preferences.PreferencesManager
import com.example.forgegym.data.local.toDomain
import com.example.forgegym.data.models.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import kotlin.math.*

class ProgressiveOverloadRepositoryImpl @Inject constructor(
    private val sessionDao: SessionDao,
    private val exerciseDao: ExerciseDao,
    private val preferencesManager: PreferencesManager
) : ProgressiveOverloadRepository {

    override fun getRecommendationForExercise(exerciseId: String): Flow<ExerciseRecommendation?> {
        return combine(
            sessionDao.getAllSessionsWithSets(),
            preferencesManager.userPreferencesFlow
        ) { sessions, preferences ->
            val exerciseSets = sessions.flatMap { it.sets }.filter { it.exerciseId == exerciseId }
            if (exerciseSets.isEmpty()) return@combine null

            val exercise = exerciseDao.getExerciseById(exerciseId)?.toDomain() ?: return@combine null

            val completedSessions = sessions.filter { it.session.endTime != null && it.sets.any { s -> s.exerciseId == exerciseId } }
                .sortedByDescending { it.session.startTime }
            
            val lastSession = completedSessions.firstOrNull() ?: return@combine null
            val lastSets = lastSession.sets.filter { it.exerciseId == exerciseId }
            val lastWeight = lastSets.maxOf { it.weight }
            val lastReps = lastSets.maxOf { it.reps }

            val bestWeight = exerciseSets.maxOf { it.weight }
            val bestReps = exerciseSets.filter { it.weight == bestWeight }.maxOf { it.reps }

            val estimated1RM = calculate1RM(bestWeight, bestReps, preferences.preferred1RMFormula)

            val strategy = preferences.preferredProgressionStrategy
            val recommendation = calculateProgression(lastWeight, lastReps, strategy)

            val plateau = detectPlateau(completedSessions, exerciseId)
            
            ExerciseRecommendation(
                exerciseId = exerciseId,
                exerciseName = exercise.name,
                suggestedWeight = recommendation.first,
                suggestedReps = recommendation.second,
                reason = generateReason(recommendation, lastWeight, lastReps, strategy, plateau),
                confidence = if (plateau) 0.6f else 0.9f,
                lastPerformance = PerformanceSnapshot(lastWeight, lastReps, lastSets.sumOf { it.weight * it.reps }, lastSession.session.startTime),
                bestPerformance = PerformanceSnapshot(bestWeight, bestReps, bestWeight * bestReps, 0L),
                estimated1RM = estimated1RM,
                plateauDetected = plateau
            )
        }
    }

    private fun generateReason(rec: Pair<Double, IntRange>, lastW: Double, lastR: Int, strategy: ProgressionStrategy, plateau: Boolean): String {
        if (plateau) return "Plateau detected. Strategy adjusted to break through."
        return when {
            rec.first > lastW -> "Last workout successful. Increasing weight for ${strategy.name.lowercase()} progression."
            rec.second.first > lastR -> "Targeting higher reps at current weight to build capacity."
            else -> "Maintaining current intensity to solidify gains."
        }
    }

    private fun calculate1RM(weight: Double, reps: Int, formula: OneRepMaxFormula): Double {
        if (reps == 0) return 0.0
        if (reps == 1) return weight
        return when (formula) {
            OneRepMaxFormula.EPLEY -> weight * (1 + reps / 30.0)
            OneRepMaxFormula.BRZYCKI -> weight * (36.0 / (37.0 - reps))
            OneRepMaxFormula.LOMBARDI -> weight * (reps.toDouble().pow(0.1))
            OneRepMaxFormula.MAYHEW -> (100 * weight) / (52.2 + 41.9 * exp(-0.055 * reps))
            OneRepMaxFormula.OCONNER -> weight * (1 + 0.025 * reps)
        }
    }

    private fun calculateProgression(lastWeight: Double, lastReps: Int, strategy: ProgressionStrategy): Pair<Double, IntRange> {
        return when (strategy) {
            ProgressionStrategy.LINEAR -> {
                if (lastReps >= 8) Pair(lastWeight + 2.5, 5..5)
                else Pair(lastWeight, 5..5)
            }
            ProgressionStrategy.DOUBLE -> {
                if (lastReps >= 12) Pair(lastWeight + 2.5, 8..8)
                else Pair(lastWeight, (lastReps + 1)..(lastReps + 1))
            }
            ProgressionStrategy.VOLUME -> {
                Pair(lastWeight, 10..12)
            }
            ProgressionStrategy.REP -> {
                Pair(lastWeight, (lastReps + 1)..(lastReps + 2))
            }
            ProgressionStrategy.PERCENTAGE -> {
                Pair(lastWeight * 1.05, 8..10)
            }
            ProgressionStrategy.TRAINING_MAX -> {
                Pair(lastWeight * 0.9, 5..5)
            }
        }
    }

    private fun detectPlateau(sessions: List<com.example.forgegym.data.local.entity.SessionWithSets>, exerciseId: String): Boolean {
        if (sessions.size < 3) return false
        val lastWeights = sessions.take(3).mapNotNull { session ->
            session.sets.filter { it.exerciseId == exerciseId }.maxOfOrNull { it.weight }
        }
        if (lastWeights.size < 3) return false
        return lastWeights.distinct().size == 1
    }

    override fun getTrainingLoadMetrics(): Flow<TrainingLoadMetrics> {
        return sessionDao.getAllSessionsWithSets().map { sessions ->
            val now = System.currentTimeMillis()
            val dayMillis = 24 * 60 * 60 * 1000L
            val acuteWindow = now - (7 * dayMillis)
            val chronicWindow = now - (28 * dayMillis)

            val acuteVolume = sessions.filter { it.session.startTime >= acuteWindow }.sumOf { s -> s.sets.sumOf { it.weight * it.reps } }
            val chronicVolume = sessions.filter { it.session.startTime >= chronicWindow }.sumOf { s -> s.sets.sumOf { it.weight * it.reps } } / 4.0

            val ratio = if (chronicVolume > 0) acuteVolume / chronicVolume else 0.0
            
            TrainingLoadMetrics(
                acuteLoad = acuteVolume,
                chronicLoad = chronicVolume,
                stressRatio = ratio,
                fatigueScore = (ratio * 50).toInt().coerceIn(0, 100),
                recoveryStatus = when {
                    ratio > 1.5 -> RecoveryStatus.LOW
                    ratio > 1.2 -> RecoveryStatus.MODERATE
                    else -> RecoveryStatus.HIGH
                }
            )
        }
    }

    override fun getPlateauDetection(exerciseId: String): Flow<Boolean> {
        return sessionDao.getAllSessionsWithSets().map { detectPlateau(it, exerciseId) }
    }

    override fun getInsights(): Flow<List<TrainingInsight>> {
        return combine(
            getTrainingLoadMetrics(),
            sessionDao.getAllSessionsWithSets()
        ) { metrics, sessions ->
            val insights = mutableListOf<TrainingInsight>()
            if (metrics.stressRatio > 1.5) {
                insights.add(TrainingInsight("High overtraining risk. Acute load is 50% higher than chronic. Deload recommended.", InsightType.DELOAD))
            }
            if (metrics.stressRatio < 0.7 && sessions.isNotEmpty()) {
                insights.add(TrainingInsight("Undertraining detected. Current load is below recovery baseline.", InsightType.VOLUME_TREND))
            }
            
            // Check for exercise specific plateaus
            // (In real app, we'd iterate over common exercises)
            
            insights
        }
    }

    override fun getWorkoutSuggestions(): Flow<List<ExerciseRecommendation>> {
        return combine(
            sessionDao.getAllSessionsWithSets(),
            exerciseDao.getAllExercises(),
            preferencesManager.userPreferencesFlow
        ) { sessions, exerciseEntities, preferences ->
            val exercises = exerciseEntities.map { it.toDomain() }
            val completed = sessions.filter { it.session.endTime != null }
            
            // Recommend 3 exercises: either not trained in 7+ days or most trained
            val suggestions = mutableListOf<ExerciseRecommendation>()
            
            for (exercise in exercises.take(10)) { // Limit scan
                val recommendation = getRecommendationForExerciseSync(exercise.id, sessions, preferences)
                if (recommendation != null) {
                    suggestions.add(recommendation)
                }
                if (suggestions.size >= 3) break
            }
            suggestions
        }
    }

    private suspend fun getRecommendationForExerciseSync(
        exerciseId: String, 
        sessions: List<com.example.forgegym.data.local.entity.SessionWithSets>,
        preferences: UserPreferences
    ): ExerciseRecommendation? {
        val exerciseSets = sessions.flatMap { it.sets }.filter { it.exerciseId == exerciseId }
        if (exerciseSets.isEmpty()) return null

        val exercise = exerciseDao.getExerciseById(exerciseId)?.toDomain() ?: return null

        val completedSessions = sessions.filter { it.session.endTime != null && it.sets.any { s -> s.exerciseId == exerciseId } }
            .sortedByDescending { it.session.startTime }
        
        val lastSession = completedSessions.firstOrNull() ?: return null
        val lastSets = lastSession.sets.filter { it.exerciseId == exerciseId }
        val lastWeight = lastSets.maxOf { it.weight }
        val lastReps = lastSets.maxOf { it.reps }

        val bestWeight = exerciseSets.maxOf { it.weight }
        val bestReps = exerciseSets.filter { it.weight == bestWeight }.maxOf { it.reps }

        val estimated1RM = calculate1RM(bestWeight, bestReps, preferences.preferred1RMFormula)
        val strategy = preferences.preferredProgressionStrategy
        val recommendation = calculateProgression(lastWeight, lastReps, strategy)
        val plateau = detectPlateau(completedSessions, exerciseId)

        return ExerciseRecommendation(
            exerciseId = exerciseId,
            exerciseName = exercise.name,
            suggestedWeight = recommendation.first,
            suggestedReps = recommendation.second,
            reason = generateReason(recommendation, lastWeight, lastReps, strategy, plateau),
            confidence = if (plateau) 0.6f else 0.9f,
            lastPerformance = PerformanceSnapshot(lastWeight, lastReps, lastSets.sumOf { it.weight * it.reps }, lastSession.session.startTime),
            bestPerformance = PerformanceSnapshot(bestWeight, bestReps, bestWeight * bestReps, 0L),
            estimated1RM = estimated1RM,
            plateauDetected = plateau
        )
    }
}

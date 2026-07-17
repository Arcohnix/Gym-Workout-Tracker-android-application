package com.example.forgegym.data.models

enum class ProgressionStrategy {
    LINEAR,
    DOUBLE,
    VOLUME,
    REP,
    PERCENTAGE,
    TRAINING_MAX
}

enum class OneRepMaxFormula {
    EPLEY,
    BRZYCKI,
    LOMBARDI,
    MAYHEW,
    OCONNER
}

data class ExerciseRecommendation(
    val exerciseId: String,
    val exerciseName: String,
    val suggestedWeight: Double,
    val suggestedReps: IntRange,
    val reason: String,
    val confidence: Float, // 0..1
    val lastPerformance: PerformanceSnapshot?,
    val bestPerformance: PerformanceSnapshot?,
    val estimated1RM: Double,
    val plateauDetected: Boolean = false,
    val deloadRecommended: Boolean = false
)

data class PerformanceSnapshot(
    val weight: Double,
    val reps: Int,
    val volume: Double,
    val date: Long
)

data class TrainingLoadMetrics(
    val acuteLoad: Double,
    val chronicLoad: Double,
    val stressRatio: Double,
    val fatigueScore: Int, // 0..100
    val recoveryStatus: RecoveryStatus
)

enum class RecoveryStatus {
    LOW, MODERATE, HIGH
}

data class TrainingInsight(
    val message: String,
    val type: InsightType
)

enum class InsightType {
    IMPROVEMENT, PLATEAU, DELOAD, VOLUME_TREND
}

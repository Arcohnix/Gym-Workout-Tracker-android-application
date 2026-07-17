package com.example.forgegym.data.models

import java.util.UUID

/**
 * Represents a single physical exercise (e.g., Bench Press).
 */
data class Exercise(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val primaryMuscleGroup: MuscleGroup,
    val secondaryMuscleGroups: List<MuscleGroup> = emptyList(),
    val equipment: Equipment = Equipment.OTHER,
    val difficulty: Difficulty = Difficulty.INTERMEDIATE,
    val category: WorkoutCategory = WorkoutCategory.STRENGTH,
    val exerciseType: ExerciseType = ExerciseType.COMPOUND,
    val forceType: ForceType = ForceType.PUSH,
    val mechanics: Mechanics = Mechanics.COMPOUND,
    val instructions: List<String> = emptyList(),
    val commonMistakes: List<String> = emptyList(),
    val tips: List<String> = emptyList(),
    val restRecommendationSeconds: Int = 60,
    val estimatedCaloriesBurnedPerMin: Double = 5.0,
    val recommendedRepRange: IntRange = 8..12,
    val recommendedSetRange: IntRange = 3..4,
    val isBeginnerFriendly: Boolean = true,
    val videoUrl: String? = null,
    val isCustom: Boolean = false,
    val isFavorite: Boolean = false,
    val notes: String = ""
)

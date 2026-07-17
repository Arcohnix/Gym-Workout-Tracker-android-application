package com.example.forgegym.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.forgegym.data.models.*
import kotlinx.serialization.Serializable

@Entity(
    tableName = "exercises",
    indices = [
        Index(value = ["name"]),
        Index(value = ["primaryMuscleGroup"]),
        Index(value = ["equipment"]),
        Index(value = ["difficulty"])
    ]
)
@Serializable
data class ExerciseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val primaryMuscleGroup: MuscleGroup,
    val secondaryMuscleGroups: List<MuscleGroup>,
    val equipment: Equipment,
    val difficulty: Difficulty,
    val category: WorkoutCategory,
    val exerciseType: ExerciseType,
    val forceType: ForceType,
    val mechanics: Mechanics,
    val instructions: List<String>,
    val commonMistakes: List<String>,
    val tips: List<String>,
    val restRecommendationSeconds: Int,
    val estimatedCaloriesBurnedPerMin: Double,
    val minRecommendedReps: Int,
    val maxRecommendedReps: Int,
    val minRecommendedSets: Int,
    val maxRecommendedSets: Int,
    val isBeginnerFriendly: Boolean,
    val videoUrl: String?,
    val isCustom: Boolean,
    val isFavorite: Boolean,
    val notes: String
)

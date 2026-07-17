package com.example.forgegym.data.repository

import com.example.forgegym.data.models.*
import kotlinx.coroutines.flow.Flow

interface ProgressiveOverloadRepository {
    fun getRecommendationForExercise(exerciseId: String): Flow<ExerciseRecommendation?>
    fun getTrainingLoadMetrics(): Flow<TrainingLoadMetrics>
    fun getPlateauDetection(exerciseId: String): Flow<Boolean>
    fun getInsights(): Flow<List<TrainingInsight>>
    fun getWorkoutSuggestions(): Flow<List<ExerciseRecommendation>>
}

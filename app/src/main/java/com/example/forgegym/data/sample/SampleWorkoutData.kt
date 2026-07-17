package com.example.forgegym.data.sample

import com.example.forgegym.data.models.Difficulty
import com.example.forgegym.data.models.Exercise
import com.example.forgegym.data.models.MuscleGroup
import com.example.forgegym.data.models.Workout
import com.example.forgegym.data.models.WorkoutCategory
import com.example.forgegym.data.models.WorkoutExercise

object SampleWorkoutData {
    val exercises = listOf(
        Exercise(
            id = "1", 
            name = "Bench Press", 
            primaryMuscleGroup = MuscleGroup.CHEST, 
            category = WorkoutCategory.STRENGTH,
            instructions = listOf("Lower the bar to your chest and press up.")
        ),
        Exercise(
            id = "2", 
            name = "Squat", 
            primaryMuscleGroup = MuscleGroup.QUADRICEPS, 
            category = WorkoutCategory.STRENGTH,
            instructions = listOf("Squat down until thighs are parallel to floor.")
        ),
        Exercise(
            id = "3", 
            name = "Deadlift", 
            primaryMuscleGroup = MuscleGroup.BACK, 
            category = WorkoutCategory.STRENGTH,
            instructions = listOf("Lift the bar from the floor to hip height.")
        ),
        Exercise(
            id = "4", 
            name = "Overhead Press", 
            primaryMuscleGroup = MuscleGroup.SHOULDERS, 
            category = WorkoutCategory.STRENGTH
        ),
        Exercise(
            id = "5", 
            name = "Pull Up", 
            primaryMuscleGroup = MuscleGroup.BACK, 
            category = WorkoutCategory.BODYWEIGHT
        )
    )

    val workouts = listOf(
        Workout(
            id = "1", 
            name = "Push Day", 
            description = "Chest, Shoulders, and Triceps focus", 
            category = WorkoutCategory.HYPERTROPHY,
            difficulty = Difficulty.INTERMEDIATE,
            workoutExercises = listOf(
                WorkoutExercise(exercise = exercises[0], order = 0),
                WorkoutExercise(exercise = exercises[3], order = 1)
            ),
            estimatedDurationMinutes = 45
        ),
        Workout(
            id = "2", 
            name = "Leg Day", 
            description = "Heavy compound movements for legs", 
            category = WorkoutCategory.STRENGTH,
            difficulty = Difficulty.ADVANCED,
            workoutExercises = listOf(
                WorkoutExercise(exercise = exercises[1], order = 0)
            ),
            estimatedDurationMinutes = 60
        ),
        Workout(
            id = "3", 
            name = "Full Body A", 
            description = "Basic strength training", 
            category = WorkoutCategory.STRENGTH,
            difficulty = Difficulty.BEGINNER,
            workoutExercises = exercises.take(3).mapIndexed { index, ex -> 
                WorkoutExercise(exercise = ex, order = index)
            },
            estimatedDurationMinutes = 75
        ),
        Workout(
            id = "4", 
            name = "Bodyweight Basics", 
            description = "No equipment needed", 
            category = WorkoutCategory.BODYWEIGHT,
            difficulty = Difficulty.BEGINNER,
            workoutExercises = listOf(
                WorkoutExercise(exercise = exercises[4], order = 0)
            ),
            estimatedDurationMinutes = 30
        )
    )
}

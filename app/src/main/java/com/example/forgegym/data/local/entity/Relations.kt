package com.example.forgegym.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * POJO to handle Workout -> Workout Exercises -> Exercise relationship.
 */
data class WorkoutWithExercises(
    @Embedded val workout: WorkoutEntity,
    @Relation(
        entity = WorkoutExerciseCrossRef::class,
        parentColumn = "id",
        entityColumn = "workoutId"
    )
    val exercises: List<WorkoutExerciseWithExercise>
)

data class WorkoutExerciseWithExercise(
    @Embedded val crossRef: WorkoutExerciseCrossRef,
    @Relation(
        parentColumn = "exerciseId",
        entityColumn = "id"
    )
    val exercise: ExerciseEntity
)

/**
 * POJO to handle Session -> Sets relationship.
 */
data class SessionWithSets(
    @Embedded val session: WorkoutSessionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionId"
    )
    val sets: List<CompletedSetEntity>
)

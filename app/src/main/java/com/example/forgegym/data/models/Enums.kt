package com.example.forgegym.data.models

import kotlinx.serialization.Serializable

@Serializable
enum class ExerciseStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED
}

@Serializable
enum class WorkoutCategory {
    STRENGTH,
    HYPERTROPHY,
    ENDURANCE,
    POWERLIFTING,
    BODYWEIGHT,
    CARDIO,
    CUSTOM,
    ALL
}

@Serializable
enum class MuscleGroup {
    CHEST,
    BACK,
    SHOULDERS,
    BICEPS,
    TRICEPS,
    FOREARMS,
    QUADRICEPS,
    HAMSTRINGS,
    GLUTES,
    CALVES,
    CORE,
    FULL_BODY,
    CARDIO,
    MOBILITY,
    OTHER,
    ALL
}

@Serializable
enum class Difficulty {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
    ALL
}

@Serializable
enum class Equipment {
    BARBELL,
    DUMBBELL,
    MACHINE,
    CABLE,
    SMITH_MACHINE,
    EZ_BAR,
    BAND,
    KETTLEBELL,
    BODYWEIGHT,
    TRAP_BAR,
    MEDICINE_BALL,
    SUSPENSION,
    OTHER,
    ALL
}

@Serializable
enum class ForceType {
    PUSH,
    PULL,
    STATIC,
    OTHER
}

@Serializable
enum class ExerciseType {
    COMPOUND,
    ISOLATION,
    OTHER
}

@Serializable
enum class Mechanics {
    COMPOUND,
    ISOLATION,
    OTHER
}

@Serializable
enum class PRType {
    MAX_WEIGHT,
    MAX_VOLUME,
    MAX_REPS,
    ESTIMATED_1RM
}

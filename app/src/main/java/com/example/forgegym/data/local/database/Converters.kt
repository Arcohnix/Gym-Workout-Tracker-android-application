package com.example.forgegym.data.local.database

import androidx.room.TypeConverter
import com.example.forgegym.data.models.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromMuscleGroup(value: MuscleGroup): String = value.name

    @TypeConverter
    fun toMuscleGroup(value: String): MuscleGroup = MuscleGroup.valueOf(value)

    @TypeConverter
    fun fromWorkoutCategory(value: WorkoutCategory): String = value.name

    @TypeConverter
    fun toWorkoutCategory(value: String): WorkoutCategory = WorkoutCategory.valueOf(value)

    @TypeConverter
    fun fromExerciseStatus(value: ExerciseStatus): String = value.name

    @TypeConverter
    fun toExerciseStatus(value: String): ExerciseStatus = ExerciseStatus.valueOf(value)
    
    @TypeConverter
    fun fromPRType(value: PRType): String = value.name

    @TypeConverter
    fun toPRType(value: String): PRType = PRType.valueOf(value)

    @TypeConverter
    fun fromDifficulty(value: Difficulty): String = value.name

    @TypeConverter
    fun toDifficulty(value: String): Difficulty = Difficulty.valueOf(value)

    @TypeConverter
    fun fromEquipment(value: Equipment): String = value.name

    @TypeConverter
    fun toEquipment(value: String): Equipment = Equipment.valueOf(value)

    @TypeConverter
    fun fromForceType(value: ForceType): String = value.name

    @TypeConverter
    fun toForceType(value: String): ForceType = ForceType.valueOf(value)

    @TypeConverter
    fun fromExerciseType(value: ExerciseType): String = value.name

    @TypeConverter
    fun toExerciseType(value: String): ExerciseType = ExerciseType.valueOf(value)

    @TypeConverter
    fun fromMechanics(value: Mechanics): String = value.name

    @TypeConverter
    fun toMechanics(value: String): Mechanics = Mechanics.valueOf(value)

    @TypeConverter
    fun fromMuscleGroupList(value: List<MuscleGroup>): String {
        return value.joinToString(",") { it.name }
    }

    @TypeConverter
    fun toMuscleGroupList(value: String): List<MuscleGroup> {
        if (value.isEmpty()) return emptyList()
        return value.split(",").map { MuscleGroup.valueOf(it) }
    }

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return try {
            Json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }
}

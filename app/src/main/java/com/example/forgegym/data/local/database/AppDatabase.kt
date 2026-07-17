package com.example.forgegym.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.forgegym.data.local.dao.*
import com.example.forgegym.data.local.entity.*

@Database(
    entities = [
        WorkoutEntity::class,
        ExerciseEntity::class,
        WorkoutExerciseCrossRef::class,
        WorkoutSessionEntity::class,
        CompletedSetEntity::class,
        PersonalRecordEntity::class,
        BodyWeightEntity::class,
        UserProfileEntity::class,
        BodyMeasurementEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun sessionDao(): SessionDao
    abstract fun personalRecordDao(): PersonalRecordDao
    abstract fun bodyWeightDao(): BodyWeightDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun bodyMeasurementDao(): BodyMeasurementDao
}

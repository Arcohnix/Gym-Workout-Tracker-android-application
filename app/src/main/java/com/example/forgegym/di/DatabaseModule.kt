package com.example.forgegym.di

import android.content.Context
import androidx.room.Room
import com.example.forgegym.data.local.dao.*
import com.example.forgegym.data.local.database.AppDatabase
import com.example.forgegym.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            Constants.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideWorkoutDao(database: AppDatabase): WorkoutDao = database.workoutDao()

    @Provides
    fun provideExerciseDao(database: AppDatabase): ExerciseDao = database.exerciseDao()

    @Provides
    fun provideSessionDao(database: AppDatabase): SessionDao = database.sessionDao()

    @Provides
    fun provideBodyWeightDao(database: AppDatabase): BodyWeightDao = database.bodyWeightDao()

    @Provides
    fun providePersonalRecordDao(database: AppDatabase): PersonalRecordDao = database.personalRecordDao()

    @Provides
    fun provideUserProfileDao(database: AppDatabase): UserProfileDao = database.userProfileDao()

    @Provides
    fun provideBodyMeasurementDao(database: AppDatabase): BodyMeasurementDao = database.bodyMeasurementDao()
}

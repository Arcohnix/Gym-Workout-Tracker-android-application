package com.example.forgegym.data.repository

import com.example.forgegym.data.models.BodyMeasurement
import com.example.forgegym.data.models.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getUserProfile(): Flow<UserProfile>
    suspend fun saveUserProfile(profile: UserProfile)
    fun getBodyMeasurements(): Flow<List<BodyMeasurement>>
    suspend fun logBodyMeasurement(measurement: BodyMeasurement)
}

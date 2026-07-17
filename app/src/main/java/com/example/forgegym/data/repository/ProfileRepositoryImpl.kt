package com.example.forgegym.data.repository

import com.example.forgegym.data.local.dao.BodyMeasurementDao
import com.example.forgegym.data.local.dao.BodyWeightDao
import com.example.forgegym.data.local.dao.UserProfileDao
import com.example.forgegym.data.local.toDomain
import com.example.forgegym.data.local.toEntity
import com.example.forgegym.data.models.BodyMeasurement
import com.example.forgegym.data.models.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val profileDao: UserProfileDao,
    private val bodyWeightDao: BodyWeightDao,
    private val measurementDao: BodyMeasurementDao
) : ProfileRepository {

    override fun getUserProfile(): Flow<UserProfile> {
        return profileDao.getUserProfile().map { profileEntity ->
            profileEntity?.toDomain() ?: UserProfile()
        }
    }

    override suspend fun saveUserProfile(profile: UserProfile) {
        profileDao.insertProfile(profile.toEntity())
    }

    override fun getBodyMeasurements(): Flow<List<BodyMeasurement>> {
        return measurementDao.getAllMeasurements().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun logBodyMeasurement(measurement: BodyMeasurement) {
        measurementDao.insertMeasurement(measurement.toEntity())
    }
}

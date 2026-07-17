package com.example.forgegym.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "user_profile")
@Serializable
data class UserProfileEntity(
    @PrimaryKey val id: Int = 0, // Single user for now
    val name: String,
    val dateOfBirth: Long, // Epoch millis
    val height: Double,
    val currentWeight: Double,
    val goalWeight: Double,
    val trainingGoal: String, // e.g. "BUILD_MUSCLE", "WEIGHT_LOSS"
    val profilePictureUrl: String? = null
)

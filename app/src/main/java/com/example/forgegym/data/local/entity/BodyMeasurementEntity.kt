package com.example.forgegym.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "body_measurements")
@Serializable
data class BodyMeasurementEntity(
    @PrimaryKey val id: String,
    val date: Long,
    val neck: Double?,
    val chest: Double?,
    val shoulders: Double?,
    val leftArm: Double?,
    val rightArm: Double?,
    val waist: Double?,
    val hips: Double?,
    val leftThigh: Double?,
    val rightThigh: Double?,
    val leftCalf: Double?,
    val rightCalf: Double?
)

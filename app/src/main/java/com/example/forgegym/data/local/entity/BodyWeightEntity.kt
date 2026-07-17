package com.example.forgegym.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "body_weight")
data class BodyWeightEntity(
    @PrimaryKey val id: String,
    val weight: Double,
    val date: Long
)

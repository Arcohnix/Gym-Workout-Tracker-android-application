package com.example.forgegym.data.models

import java.util.Calendar

enum class TrainingGoal {
    WEIGHT_LOSS, BUILD_MUSCLE, FITNESS, STRENGTH, ENDURANCE
}

data class UserProfile(
    val name: String = "",
    val dateOfBirth: Long = 0L,
    val height: Double = 0.0, // in cm
    val currentWeight: Double = 0.0, // in kg
    val goalWeight: Double = 0.0, // in kg
    val trainingGoal: TrainingGoal = TrainingGoal.FITNESS,
    val profilePictureUrl: String? = null
) {
    val bmi: Double
        get() = if (height > 0) currentWeight / ((height / 100) * (height / 100)) else 0.0

    val age: Int
        get() {
            if (dateOfBirth == 0L) return 0
            val today = Calendar.getInstance()
            val dob = Calendar.getInstance().apply { timeInMillis = dateOfBirth }
            var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
            if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
                age--
            }
            return age
        }
}

data class BodyMeasurement(
    val id: String,
    val date: Long,
    val neck: Double? = null,
    val chest: Double? = null,
    val shoulders: Double? = null,
    val leftArm: Double? = null,
    val rightArm: Double? = null,
    val waist: Double? = null,
    val hips: Double? = null,
    val leftThigh: Double? = null,
    val rightThigh: Double? = null,
    val leftCalf: Double? = null,
    val rightCalf: Double? = null
)

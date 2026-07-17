package com.example.forgegym.data.models

enum class WeightUnit {
    KG, LBS
}

enum class HeightUnit {
    CM, FT_IN
}

enum class ThemeMode {
    SYSTEM, DARK, LIGHT
}

data class UserPreferences(
    val weightUnit: WeightUnit = WeightUnit.KG,
    val heightUnit: HeightUnit = HeightUnit.CM,
    val themeMode: ThemeMode = ThemeMode.DARK,
    val isAmoledMode: Boolean = true,
    val isDynamicColorEnabled: Boolean = false,
    val defaultRestTimerSeconds: Int = 60,
    val isAutoStartRestTimer: Boolean = true,
    val isAutoResumeWorkout: Boolean = true,
    val isConfirmEndWorkout: Boolean = true,
    val preferredProgressionStrategy: ProgressionStrategy = ProgressionStrategy.DOUBLE,
    val preferred1RMFormula: OneRepMaxFormula = OneRepMaxFormula.BRZYCKI,
    val isVibrationEnabled: Boolean = true,
    val isSoundEnabled: Boolean = true,
    val defaultWorkoutId: String? = null,
    val weeklyGoal: Int = 4
)

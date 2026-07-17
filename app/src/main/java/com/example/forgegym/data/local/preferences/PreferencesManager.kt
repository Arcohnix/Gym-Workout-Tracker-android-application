package com.example.forgegym.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.forgegym.data.models.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val WEIGHT_UNIT = stringPreferencesKey("weight_unit")
        val HEIGHT_UNIT = stringPreferencesKey("height_unit")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val AMOLED_MODE = booleanPreferencesKey("amoled_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val REST_TIMER = intPreferencesKey("default_rest_timer")
        val AUTO_START_REST = booleanPreferencesKey("auto_start_rest")
        val AUTO_RESUME_WORKOUT = booleanPreferencesKey("auto_resume_workout")
        val CONFIRM_END_WORKOUT = booleanPreferencesKey("confirm_end_workout")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val DEFAULT_WORKOUT_ID = stringPreferencesKey("default_workout_id")
        val WEEKLY_GOAL = intPreferencesKey("weekly_goal")
        val PROGRESSION_STRATEGY = stringPreferencesKey("progression_strategy")
        val ONE_REP_MAX_FORMULA = stringPreferencesKey("one_rep_max_formula")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .map { preferences ->
            UserPreferences(
                weightUnit = WeightUnit.valueOf(preferences[PreferencesKeys.WEIGHT_UNIT] ?: WeightUnit.KG.name),
                heightUnit = HeightUnit.valueOf(preferences[PreferencesKeys.HEIGHT_UNIT] ?: HeightUnit.CM.name),
                themeMode = ThemeMode.valueOf(preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.DARK.name),
                isAmoledMode = preferences[PreferencesKeys.AMOLED_MODE] ?: true,
                isDynamicColorEnabled = preferences[PreferencesKeys.DYNAMIC_COLOR] ?: false,
                defaultRestTimerSeconds = preferences[PreferencesKeys.REST_TIMER] ?: 60,
                isAutoStartRestTimer = preferences[PreferencesKeys.AUTO_START_REST] ?: true,
                isAutoResumeWorkout = preferences[PreferencesKeys.AUTO_RESUME_WORKOUT] ?: true,
                isConfirmEndWorkout = preferences[PreferencesKeys.CONFIRM_END_WORKOUT] ?: true,
                isVibrationEnabled = preferences[PreferencesKeys.VIBRATION_ENABLED] ?: true,
                isSoundEnabled = preferences[PreferencesKeys.SOUND_ENABLED] ?: true,
                defaultWorkoutId = preferences[PreferencesKeys.DEFAULT_WORKOUT_ID],
                weeklyGoal = preferences[PreferencesKeys.WEEKLY_GOAL] ?: 4,
                preferredProgressionStrategy = ProgressionStrategy.valueOf(
                    preferences[PreferencesKeys.PROGRESSION_STRATEGY] ?: ProgressionStrategy.DOUBLE.name
                ),
                preferred1RMFormula = OneRepMaxFormula.valueOf(
                    preferences[PreferencesKeys.ONE_REP_MAX_FORMULA] ?: OneRepMaxFormula.BRZYCKI.name
                )
            )
        }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { it[PreferencesKeys.ONBOARDING_COMPLETED] ?: false }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun updateWeightUnit(unit: WeightUnit) {
        context.dataStore.edit { it[PreferencesKeys.WEIGHT_UNIT] = unit.name }
    }

    suspend fun updateHeightUnit(unit: HeightUnit) {
        context.dataStore.edit { it[PreferencesKeys.HEIGHT_UNIT] = unit.name }
    }

    suspend fun updateThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[PreferencesKeys.THEME_MODE] = mode.name }
    }

    suspend fun updateAmoledMode(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.AMOLED_MODE] = enabled }
    }

    suspend fun updateDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.DYNAMIC_COLOR] = enabled }
    }

    suspend fun updateRestTimer(seconds: Int) {
        context.dataStore.edit { it[PreferencesKeys.REST_TIMER] = seconds }
    }

    suspend fun updateAutoStartRestTimer(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.AUTO_START_REST] = enabled }
    }

    suspend fun updateAutoResumeWorkout(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.AUTO_RESUME_WORKOUT] = enabled }
    }

    suspend fun updateConfirmEndWorkout(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.CONFIRM_END_WORKOUT] = enabled }
    }

    suspend fun updateVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.VIBRATION_ENABLED] = enabled }
    }

    suspend fun updateSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SOUND_ENABLED] = enabled }
    }

    suspend fun updateDefaultWorkoutId(id: String?) {
        context.dataStore.edit { 
            if (id != null) it[PreferencesKeys.DEFAULT_WORKOUT_ID] = id
            else it.remove(PreferencesKeys.DEFAULT_WORKOUT_ID)
        }
    }

    suspend fun updateWeeklyGoal(goal: Int) {
        context.dataStore.edit { it[PreferencesKeys.WEEKLY_GOAL] = goal }
    }

    suspend fun updateProgressionStrategy(strategy: ProgressionStrategy) {
        context.dataStore.edit { it[PreferencesKeys.PROGRESSION_STRATEGY] = strategy.name }
    }

    suspend fun updateOneRepMaxFormula(formula: OneRepMaxFormula) {
        context.dataStore.edit { it[PreferencesKeys.ONE_REP_MAX_FORMULA] = formula.name }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}

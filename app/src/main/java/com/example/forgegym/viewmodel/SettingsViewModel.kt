package com.example.forgegym.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.forgegym.data.local.preferences.PreferencesManager
import com.example.forgegym.data.models.*
import com.example.forgegym.data.repository.BackupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val preferences: UserPreferences = UserPreferences(),
    val backupJson: String? = null,
    val historyCsv: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class SettingsUiEvent {
    data class OnWeightUnitChanged(val unit: WeightUnit) : SettingsUiEvent()
    data class OnHeightUnitChanged(val unit: HeightUnit) : SettingsUiEvent()
    data class OnThemeModeChanged(val mode: ThemeMode) : SettingsUiEvent()
    data class OnAmoledToggle(val enabled: Boolean) : SettingsUiEvent()
    data class OnDynamicColorToggle(val enabled: Boolean) : SettingsUiEvent()
    data class OnRestTimerChanged(val seconds: Int) : SettingsUiEvent()
    data class OnAutoStartRestToggle(val enabled: Boolean) : SettingsUiEvent()
    data class OnAutoResumeToggle(val enabled: Boolean) : SettingsUiEvent()
    data class OnConfirmEndToggle(val enabled: Boolean) : SettingsUiEvent()
    data class OnProgressionStrategyChanged(val strategy: ProgressionStrategy) : SettingsUiEvent()
    data class On1RMFormulaChanged(val formula: OneRepMaxFormula) : SettingsUiEvent()
    data class OnVibrationToggle(val enabled: Boolean) : SettingsUiEvent()
    data class OnSoundToggle(val enabled: Boolean) : SettingsUiEvent()
    data class OnRestoreData(val json: String) : SettingsUiEvent()
    object OnBackupClick : SettingsUiEvent()
    object OnExportCsvClick : SettingsUiEvent()
    object OnDeleteAllData : SettingsUiEvent()
    object OnResetApp : SettingsUiEvent()
    object ClearBackupState : SettingsUiEvent()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val backupRepository: BackupRepository
) : BaseViewModel<SettingsUiState, SettingsUiEvent>(SettingsUiState(isLoading = true)) {

    override val state: StateFlow<SettingsUiState> = preferencesManager.userPreferencesFlow
        .map { preferences ->
            SettingsUiState(preferences = preferences, isLoading = false)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsUiState(isLoading = true)
        )

    override fun onEvent(event: SettingsUiEvent) {
        viewModelScope.launch {
            when (event) {
                is SettingsUiEvent.OnWeightUnitChanged -> preferencesManager.updateWeightUnit(event.unit)
                is SettingsUiEvent.OnHeightUnitChanged -> preferencesManager.updateHeightUnit(event.unit)
                is SettingsUiEvent.OnThemeModeChanged -> preferencesManager.updateThemeMode(event.mode)
                is SettingsUiEvent.OnAmoledToggle -> preferencesManager.updateAmoledMode(event.enabled)
                is SettingsUiEvent.OnDynamicColorToggle -> preferencesManager.updateDynamicColor(event.enabled)
                is SettingsUiEvent.OnRestTimerChanged -> preferencesManager.updateRestTimer(event.seconds)
                is SettingsUiEvent.OnAutoStartRestToggle -> preferencesManager.updateAutoStartRestTimer(event.enabled)
                is SettingsUiEvent.OnAutoResumeToggle -> preferencesManager.updateAutoResumeWorkout(event.enabled)
                is SettingsUiEvent.OnConfirmEndToggle -> preferencesManager.updateConfirmEndWorkout(event.enabled)
                is SettingsUiEvent.OnProgressionStrategyChanged -> preferencesManager.updateProgressionStrategy(event.strategy)
                is SettingsUiEvent.On1RMFormulaChanged -> preferencesManager.updateOneRepMaxFormula(event.formula)
                is SettingsUiEvent.OnVibrationToggle -> preferencesManager.updateVibrationEnabled(event.enabled)
                is SettingsUiEvent.OnSoundToggle -> preferencesManager.updateSoundEnabled(event.enabled)
                is SettingsUiEvent.OnRestoreData -> {
                    backupRepository.importBackup(event.json)
                }
                SettingsUiEvent.OnBackupClick -> {
                    val json = backupRepository.exportBackup()
                    updateState { it.copy(backupJson = json) }
                }
                SettingsUiEvent.OnExportCsvClick -> {
                    val csv = backupRepository.exportHistoryToCsv()
                    updateState { it.copy(historyCsv = csv) }
                }
                SettingsUiEvent.OnDeleteAllData -> backupRepository.deleteAllData()
                SettingsUiEvent.OnResetApp -> backupRepository.resetApp()
                SettingsUiEvent.ClearBackupState -> updateState { it.copy(backupJson = null, historyCsv = null) }
            }
        }
    }
}

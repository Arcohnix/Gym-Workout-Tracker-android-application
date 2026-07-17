package com.example.forgegym.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.forgegym.data.models.BodyMeasurement
import com.example.forgegym.data.models.UserProfile
import com.example.forgegym.data.repository.HistoryRepository
import com.example.forgegym.data.repository.ProfileRepository
import com.example.forgegym.data.repository.ProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val userProfile: UserProfile = UserProfile(),
    val latestMeasurement: BodyMeasurement? = null,
    val totalWorkouts: Int = 0,
    val totalVolume: Double = 0.0,
    val totalTimeMinutes: Long = 0,
    val prCount: Int = 0,
    val isLoading: Boolean = false
)

sealed class ProfileUiEvent {
    data class OnUpdateProfile(val profile: UserProfile) : ProfileUiEvent()
    data class OnLogMeasurement(val measurement: BodyMeasurement) : ProfileUiEvent()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val historyRepository: HistoryRepository,
    private val progressRepository: ProgressRepository
) : BaseViewModel<ProfileUiState, ProfileUiEvent>(ProfileUiState(isLoading = true)) {

    override val state: StateFlow<ProfileUiState> = combine(
        profileRepository.getUserProfile(),
        profileRepository.getBodyMeasurements(),
        historyRepository.getSessionHistory(),
        progressRepository.getPersonalRecords()
    ) { profile, measurements, history, prs ->
        val completedSessions = history.filter { it.endTime != null }
        val totalTime = completedSessions.sumOf { (it.endTime!! - it.startTime) / (1000 * 60) }

        ProfileUiState(
            userProfile = profile,
            latestMeasurement = measurements.firstOrNull(),
            totalWorkouts = completedSessions.size,
            totalVolume = completedSessions.sumOf { it.totalVolume },
            totalTimeMinutes = totalTime,
            prCount = prs.size,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileUiState(isLoading = true)
    )

    override fun onEvent(event: ProfileUiEvent) {
        when (event) {
            is ProfileUiEvent.OnUpdateProfile -> {
                viewModelScope.launch {
                    profileRepository.saveUserProfile(event.profile)
                }
            }
            is ProfileUiEvent.OnLogMeasurement -> {
                viewModelScope.launch {
                    profileRepository.logBodyMeasurement(event.measurement)
                }
            }
        }
    }
}

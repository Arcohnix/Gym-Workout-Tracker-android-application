package com.example.forgegym.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.forgegym.data.local.preferences.PreferencesManager
import com.example.forgegym.data.models.TrainingGoal
import com.example.forgegym.data.models.UserProfile
import com.example.forgegym.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val name: String = "",
    val dateOfBirth: Long = 0L,
    val height: String = "",
    val weight: String = "",
    val goalWeight: String = "",
    val selectedGoal: TrainingGoal = TrainingGoal.FITNESS,
    val isOnboardingCompleted: Boolean = false
)

sealed class OnboardingUiEvent {
    data class OnNameChanged(val name: String) : OnboardingUiEvent()
    data class OnDobChanged(val dob: Long) : OnboardingUiEvent()
    data class OnHeightChanged(val height: String) : OnboardingUiEvent()
    data class OnWeightChanged(val weight: String) : OnboardingUiEvent()
    data class OnGoalWeightChanged(val weight: String) : OnboardingUiEvent()
    data class OnGoalSelected(val goal: TrainingGoal) : OnboardingUiEvent()
    object OnCompleteOnboarding : OnboardingUiEvent()
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val preferencesManager: PreferencesManager
) : BaseViewModel<OnboardingUiState, OnboardingUiEvent>(OnboardingUiState()) {

    override fun onEvent(event: OnboardingUiEvent) {
        when (event) {
            is OnboardingUiEvent.OnNameChanged -> updateState { it.copy(name = event.name) }
            is OnboardingUiEvent.OnDobChanged -> updateState { it.copy(dateOfBirth = event.dob) }
            is OnboardingUiEvent.OnHeightChanged -> updateState { it.copy(height = event.height) }
            is OnboardingUiEvent.OnWeightChanged -> updateState { it.copy(weight = event.weight) }
            is OnboardingUiEvent.OnGoalWeightChanged -> updateState { it.copy(goalWeight = event.weight) }
            is OnboardingUiEvent.OnGoalSelected -> updateState { it.copy(selectedGoal = event.goal) }
            OnboardingUiEvent.OnCompleteOnboarding -> completeOnboarding()
        }
    }

    private fun completeOnboarding() {
        viewModelScope.launch {
            val currentState = state.value
            val profile = UserProfile(
                name = currentState.name,
                dateOfBirth = currentState.dateOfBirth,
                height = currentState.height.toDoubleOrNull() ?: 0.0,
                currentWeight = currentState.weight.toDoubleOrNull() ?: 0.0,
                goalWeight = currentState.goalWeight.toDoubleOrNull() ?: 0.0,
                trainingGoal = currentState.selectedGoal
            )
            profileRepository.saveUserProfile(profile)
            preferencesManager.setOnboardingCompleted(true)
            updateState { it.copy(isOnboardingCompleted = true) }
        }
    }
}

package com.example.forgegym.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.forgegym.data.models.WorkoutSession
import com.example.forgegym.data.repository.HistoryRepository
import com.example.forgegym.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val sessions: List<WorkoutSession> = emptyList(),
    val searchQuery: String = "",
    val selectedDate: Long? = null, // Milliseconds of the start of the day
    val isLoading: Boolean = false
)

sealed class HistoryUiEvent {
    data class OnSearchQueryChanged(val query: String) : HistoryUiEvent()
    data class OnDateSelected(val date: Long?) : HistoryUiEvent()
    data class OnDeleteSession(val sessionId: String) : HistoryUiEvent()
    data class OnDuplicateSession(val sessionId: String) : HistoryUiEvent()
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val workoutRepository: WorkoutRepository
) : BaseViewModel<HistoryUiState, HistoryUiEvent>(HistoryUiState(isLoading = true)) {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedDate = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<HistoryUiState> = combine(
        _searchQuery,
        _selectedDate
    ) { query, date ->
        Pair(query, date)
    }.flatMapLatest { (query, date) ->
        val startTime = date
        val endTime = date?.let { it + 24 * 60 * 60 * 1000 - 1 }
        
        historyRepository.getSessionHistory(query, startTime, endTime)
    }.combine(_searchQuery) { sessions, query ->
        HistoryUiState(
            sessions = sessions,
            searchQuery = query,
            selectedDate = _selectedDate.value,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState(isLoading = true)
    )

    override fun onEvent(event: HistoryUiEvent) {
        when (event) {
            is HistoryUiEvent.OnSearchQueryChanged -> {
                _searchQuery.value = event.query
            }
            is HistoryUiEvent.OnDateSelected -> {
                _selectedDate.value = event.date
            }
            is HistoryUiEvent.OnDeleteSession -> {
                viewModelScope.launch {
                    historyRepository.deleteSession(event.sessionId)
                }
            }
            is HistoryUiEvent.OnDuplicateSession -> {
                viewModelScope.launch {
                    historyRepository.getSessionById(event.sessionId).first()?.let { session ->
                        workoutRepository.duplicateWorkout(session.workoutId)
                    }
                }
            }
        }
    }
}

package com.example.forgegym.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class BaseViewModel<State, Event>(initialState: State) : ViewModel() {
    protected val _state = MutableStateFlow(initialState)
    open val state: StateFlow<State> = _state.asStateFlow()

    abstract fun onEvent(event: Event)

    protected fun updateState(reducer: (State) -> State) {
        _state.value = reducer(_state.value)
    }
}

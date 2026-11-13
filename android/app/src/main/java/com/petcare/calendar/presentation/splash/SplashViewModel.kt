package com.petcare.calendar.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petcare.calendar.core.config.GameConfig
import com.petcare.calendar.core.di.PlatformActions
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SplashViewModel(platform: PlatformActions) : ViewModel() {
    private val mutable = MutableStateFlow(SplashUiState(reducedMotion = platform.systemReducedMotion()))
    val uiState = mutable.asStateFlow()
    private val _finished = MutableStateFlow(false)
    val finished = _finished.asStateFlow()
    private var consumed = false
    init {
        viewModelScope.launch {
            delay(GameConfig.LOADER_DURATION_MS)
            mutable.update { it.copy(isFinished = true) }
            _finished.value = true
        }
    }
    fun consumeFinished(): Boolean {
        if (!_finished.value || consumed) return false
        consumed = true
        return true
    }
}

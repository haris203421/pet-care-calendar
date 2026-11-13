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

data class SplashUiState(val reducedMotion: Boolean = false, val isFinished: Boolean = false)

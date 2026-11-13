package com.petcare.calendar.presentation.app

import androidx.lifecycle.viewModelScope
import com.petcare.calendar.core.di.Dependencies
import com.petcare.calendar.core.navigation.Screen
import com.petcare.calendar.core.navigation.UiEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class AppUiState(val settings: com.petcare.calendar.domain.model.AppSettings = com.petcare.calendar.domain.model.AppSettings())

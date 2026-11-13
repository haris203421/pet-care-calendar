package com.petcare.calendar.presentation.gameover

import androidx.lifecycle.viewModelScope
import com.petcare.calendar.core.di.Dependencies
import com.petcare.calendar.core.navigation.*
import com.petcare.calendar.domain.model.*
import com.petcare.calendar.presentation.app.CalendarViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class GameOverUiState(
    val loadState: LoadState = LoadState.LOADING, val summary: CareSessionSummary? = null,
    val newLogs: List<CareLog> = emptyList(), val canUndo: Boolean = false,
    val error: Boolean = false, val busy: Boolean = false
)

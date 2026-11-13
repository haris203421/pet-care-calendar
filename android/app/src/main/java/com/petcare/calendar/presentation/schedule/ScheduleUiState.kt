package com.petcare.calendar.presentation.schedule

import androidx.lifecycle.viewModelScope
import com.petcare.calendar.core.di.Dependencies
import com.petcare.calendar.core.navigation.*
import com.petcare.calendar.domain.model.*
import com.petcare.calendar.presentation.app.CalendarViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ScheduleUiState(
    val loadState: LoadState = LoadState.LOADING, val pets: List<Pet> = emptyList(),
    val petId: String? = null, val filter: Int = 0, val routines: List<CareRoutine> = emptyList(),
    val pendingArchive: String? = null, val busy: Boolean = false, val error: Boolean = false
)

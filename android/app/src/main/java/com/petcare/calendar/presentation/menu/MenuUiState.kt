package com.petcare.calendar.presentation.menu

import androidx.lifecycle.viewModelScope
import com.petcare.calendar.core.di.Dependencies
import com.petcare.calendar.core.navigation.*
import com.petcare.calendar.domain.model.*
import com.petcare.calendar.presentation.app.CalendarViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class MenuUiState(
    val loadState: LoadState = LoadState.LOADING, val pets: List<Pet> = emptyList(),
    val selectedPetId: String? = null, val due: Int = 0, val completed: Int = 0,
    val sampleMode: Boolean = false, val showSampleChoice: Boolean = false,
    val next: CareOccurrence? = null, val error: Boolean = false, val pendingDestination: String? = null
)

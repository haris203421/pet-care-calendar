package com.petcare.calendar.presentation.vetvisits

import androidx.lifecycle.viewModelScope
import com.petcare.calendar.core.di.Dependencies
import com.petcare.calendar.core.navigation.*
import com.petcare.calendar.domain.model.*
import com.petcare.calendar.presentation.app.CalendarViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate

data class VetVisitsUiState(
    val loadState: LoadState = LoadState.LOADING, val pets: List<Pet> = emptyList(),
    val petId: String? = null, val past: Boolean = false, val visits: List<VetAppointment> = emptyList(),
    val expandedId: String? = null, val pendingId: String? = null, val action: VisitAction = VisitAction.ATTEND,
    val busy: Boolean = false, val error: Boolean = false
)

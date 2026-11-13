package com.petcare.calendar.presentation.visiteditor

import androidx.lifecycle.viewModelScope
import com.petcare.calendar.R
import com.petcare.calendar.core.di.Dependencies
import com.petcare.calendar.core.navigation.*
import com.petcare.calendar.domain.model.*
import com.petcare.calendar.presentation.app.CalendarViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class VetVisitEditorUiState(
    val loadState: LoadState = LoadState.LOADING, val visit: VetAppointment = VetAppointment(petId = "", reason = ""),
    val pets: List<Pet> = emptyList(), val editing: Boolean = false, val dirty: Boolean = false,
    val busy: Boolean = false, val notFound: Boolean = false, val discard: Boolean = false, val error: Int? = null
)

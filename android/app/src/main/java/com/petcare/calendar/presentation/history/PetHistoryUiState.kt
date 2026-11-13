package com.petcare.calendar.presentation.history

import androidx.lifecycle.viewModelScope
import com.petcare.calendar.core.di.Dependencies
import com.petcare.calendar.core.navigation.*
import com.petcare.calendar.domain.model.*
import com.petcare.calendar.presentation.app.CalendarViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class PetHistoryUiState(
    val loadState: LoadState = LoadState.LOADING, val pets: List<Pet> = emptyList(),
    val petId: String? = null, val range: Int = 7, val kind: Int = 0,
    val records: List<CareLog> = emptyList(), val expandedId: String? = null
)

package com.petcare.calendar.presentation.game

import androidx.lifecycle.viewModelScope
import com.petcare.calendar.core.di.Dependencies
import com.petcare.calendar.core.navigation.*
import com.petcare.calendar.domain.model.*
import com.petcare.calendar.presentation.app.CalendarViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class GameUiState(
    val loadState: LoadState = LoadState.LOADING, val date: String = LocalDate.now().toString(),
    val pets: List<Pet> = emptyList(), val petId: String? = null,
    val items: List<CareOccurrence> = emptyList(), val selectedId: String? = null,
    val completed: Int = 0, val skipped: Int = 0, val busy: Boolean = false,
    val error: Boolean = false, val skipDialog: Boolean = false, val skipNote: String = "",
    val undoId: String? = null, val saved: Boolean = false, val newDay: Boolean = false
)

package com.petcare.calendar.presentation.peteditor

import androidx.lifecycle.viewModelScope
import com.petcare.calendar.R
import com.petcare.calendar.core.di.Dependencies
import com.petcare.calendar.core.navigation.*
import com.petcare.calendar.domain.model.*
import com.petcare.calendar.presentation.app.CalendarViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate

data class PetEditorUiState(
    val loadState: LoadState = LoadState.LOADING, val pet: Pet = Pet(name = ""),
    val editing: Boolean = false, val dirty: Boolean = false, val busy: Boolean = false,
    val notFound: Boolean = false, val discard: Boolean = false, val archive: Boolean = false,
    val error: Int? = null
)

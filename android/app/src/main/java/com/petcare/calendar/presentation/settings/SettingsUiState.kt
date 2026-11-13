package com.petcare.calendar.presentation.settings

import androidx.lifecycle.viewModelScope
import com.petcare.calendar.core.di.Dependencies
import com.petcare.calendar.core.navigation.*
import com.petcare.calendar.domain.model.*
import com.petcare.calendar.presentation.app.CalendarViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsUiState(
    val loadState: LoadState = LoadState.LOADING, val settings: AppSettings = AppSettings(),
    val archived: List<Pet> = emptyList(), val empty: Boolean = false, val permission: Boolean = false,
    val permissionPending: Boolean = false, val permissionDenied: Boolean = false,
    val pending: DataAction? = null, val busy: Boolean = false, val error: Boolean = false,
    val saved: Boolean = false
)

package com.petcare.calendar.presentation.menu

import androidx.lifecycle.viewModelScope
import com.petcare.calendar.core.di.Dependencies
import com.petcare.calendar.core.navigation.*
import com.petcare.calendar.domain.model.*
import com.petcare.calendar.presentation.app.CalendarViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class MenuViewModel(private val dependencies: Dependencies) : CalendarViewModel() {
    private val mutable = MutableStateFlow(MenuUiState())
    val uiState = mutable.asStateFlow()
    init {
        viewModelScope.launch {
            dependencies.repository.state.collect { store ->
                val items = dependencies.daily(store.data, LocalDate.now())
                mutable.update { it.copy(loadState = store.loadState, pets = store.data.activePets(), due = items.count { row -> row.log == null }, completed = items.count { row -> row.status == CareStatus.DONE }, sampleMode = store.data.settings.sampleMode, next = items.firstOrNull { row -> row.log == null }) }
            }
        }
        viewModelScope.launch { dependencies.platform.destination.collect { value -> mutable.update { it.copy(pendingDestination = value) } } }
    }
    fun select(id: String?) { mutable.update { it.copy(selectedPetId = id) } }
    fun navigate(screen: Screen) { event(UiEvent.Navigate(screen)) }
    fun play() { if (uiState.value.pets.isNotEmpty()) navigate(Screen.Game(petId = uiState.value.selectedPetId)) }
    fun sampleChoice(show: Boolean) { mutable.update { it.copy(showSampleChoice = show) } }
    fun keepSamples() {
        viewModelScope.launch {
            val result = dependencies.repository.change { it.copy(settings = it.settings.copy(sampleMode = false)) }
            mutable.update { it.copy(showSampleChoice = false, error = result.isFailure) }
        }
    }
    fun startEmpty() {
        viewModelScope.launch {
            val result = dependencies.repository.reset()
            mutable.update { it.copy(showSampleChoice = false, selectedPetId = null, error = result.isFailure) }
        }
    }
    fun openReminder() {
        val destination = uiState.value.pendingDestination
        dependencies.platform.setDestination(null)
        if (destination?.startsWith("visit:") == true) navigate(Screen.VetVisits) else play()
    }
    fun retry() { viewModelScope.launch { dependencies.repository.load() }; mutable.update { it.copy(error = false) } }
}

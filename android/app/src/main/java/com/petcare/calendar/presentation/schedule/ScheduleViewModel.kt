package com.petcare.calendar.presentation.schedule

import androidx.lifecycle.viewModelScope
import com.petcare.calendar.core.di.Dependencies
import com.petcare.calendar.core.navigation.*
import com.petcare.calendar.domain.model.*
import com.petcare.calendar.presentation.app.CalendarViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ScheduleViewModel(private val dependencies: Dependencies) : CalendarViewModel() {
    private val mutable = MutableStateFlow(ScheduleUiState())
    val uiState = mutable.asStateFlow()
    init { viewModelScope.launch { dependencies.repository.state.collect { refresh() } } }
    private fun refresh() {
        val store = dependencies.repository.state.value
        val pets = store.data.activePets()
        mutable.update { state -> state.copy(loadState = store.loadState, pets = pets, routines = store.data.routines.filter { !it.archived && pets.any { pet -> pet.id == it.petId } && (state.petId == null || it.petId == state.petId) && (state.filter == 2 || it.enabled == (state.filter == 0)) }) }
    }
    fun filterPet(id: String?) { mutable.update { it.copy(petId = id) }; refresh() }
    fun filter(value: Int) { mutable.update { it.copy(filter = value) }; refresh() }
    fun navigate(screen: Screen) { event(UiEvent.Navigate(screen)) }
    fun back() { event(UiEvent.Back) }
    fun archive(id: String?) { mutable.update { it.copy(pendingArchive = id) } }
    fun toggle(id: String, enabled: Boolean) { change { data -> data.copy(routines = data.routines.map { if (it.id == id) it.copy(enabled = enabled) else it }) } }
    fun confirmArchive() {
        val id = uiState.value.pendingArchive ?: return
        mutable.update { it.copy(pendingArchive = null) }
        change { data -> data.copy(routines = data.routines.map { if (it.id == id) it.copy(archived = true, enabled = false) else it }) }
    }
    private fun change(transform: (CalendarData) -> CalendarData) {
        if (uiState.value.busy) return
        mutable.update { it.copy(busy = true) }
        viewModelScope.launch { val result = dependencies.repository.change(transform); mutable.update { it.copy(busy = false, error = result.isFailure) } }
    }
    fun retry() { mutable.update { it.copy(error = false) }; viewModelScope.launch { dependencies.repository.load() } }
}

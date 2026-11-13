package com.petcare.calendar.presentation.history

import androidx.lifecycle.viewModelScope
import com.petcare.calendar.core.di.Dependencies
import com.petcare.calendar.core.navigation.*
import com.petcare.calendar.domain.model.*
import com.petcare.calendar.presentation.app.CalendarViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class PetHistoryViewModel(private val dependencies: Dependencies, route: Screen.PetHistory) : CalendarViewModel() {
    private val mutable = MutableStateFlow(PetHistoryUiState(petId = route.petId))
    val uiState = mutable.asStateFlow()
    init { viewModelScope.launch { dependencies.repository.state.collect { refresh() } } }
    private fun refresh() {
        val store = dependencies.repository.state.value
        mutable.update { state -> state.copy(loadState = store.loadState, pets = store.data.pets, records = store.data.logs.filter {
            (state.petId == null || it.petId == state.petId) && (state.range == 0 || LocalDate.parse(it.recordedLocal.take(10)) >= LocalDate.now().minusDays(state.range.toLong() - 1)) &&
                (state.kind == 0 || it.status == if (state.kind == 1) RecordStatus.DONE else RecordStatus.SKIPPED)
        }.sortedByDescending { it.recordedAt }) }
    }
    fun pet(id: String?) { mutable.update { it.copy(petId = id) }; refresh() }
    fun range(value: Int) { mutable.update { it.copy(range = value) }; refresh() }
    fun kind(value: Int) { mutable.update { it.copy(kind = value) }; refresh() }
    fun clearFilters() { mutable.update { it.copy(petId = null, range = 0, kind = 0) }; refresh() }
    fun expand(id: String) { mutable.update { it.copy(expandedId = if (it.expandedId == id) null else id) } }
    fun navigate(screen: Screen) { event(UiEvent.Navigate(screen)) }
    fun back() { event(UiEvent.Back) }
    fun retry() { viewModelScope.launch { dependencies.repository.load() } }
}

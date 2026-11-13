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

enum class VisitAction { ATTEND, CANCEL, REOPEN }
class VetVisitsViewModel(private val dependencies: Dependencies) : CalendarViewModel() {
    private val mutable = MutableStateFlow(VetVisitsUiState())
    val uiState = mutable.asStateFlow()
    init { viewModelScope.launch { dependencies.repository.state.collect { refresh() } } }
    private fun refresh() {
        val store = dependencies.repository.state.value
        mutable.update { state -> state.copy(loadState = store.loadState, pets = store.data.pets, visits = store.data.visits.filter { (state.petId == null || it.petId == state.petId) && ((it.status != VisitStatus.PLANNED) == state.past) }.sortedWith(if (state.past) compareByDescending<VetAppointment> { it.date }.thenByDescending { it.time } else compareBy<VetAppointment> { it.date }.thenBy { it.time })) }
    }
    fun pet(id: String?) { mutable.update { it.copy(petId = id) }; refresh() }
    fun tab(past: Boolean) { mutable.update { it.copy(past = past) }; refresh() }
    fun expand(id: String) { mutable.update { it.copy(expandedId = if (it.expandedId == id) null else id) } }
    fun navigate(screen: Screen) { event(UiEvent.Navigate(screen)) }
    fun back() { event(UiEvent.Back) }
    fun ask(id: String, action: VisitAction) { mutable.update { it.copy(pendingId = id, action = action) } }
    fun dismiss() { mutable.update { it.copy(pendingId = null) } }
    fun confirm() {
        val id = uiState.value.pendingId ?: return
        val action = uiState.value.action
        if (uiState.value.busy) return
        mutable.update { it.copy(busy = true, pendingId = null) }
        viewModelScope.launch {
            val result = dependencies.repository.change { data ->
                val visit = data.visits.find { it.id == id } ?: return@change data
                when (action) {
                    VisitAction.CANCEL -> data.copy(visits = data.visits.map { if (it.id == id) it.copy(status = VisitStatus.CANCELED, canceledAt = Instant.now().toString()) else it })
                    VisitAction.REOPEN -> dependencies.record.undo(data, "visit:$id").let { reopened -> reopened.copy(visits = reopened.visits.map { if (it.id == id) it.copy(status = VisitStatus.PLANNED, attendedAt = null, canceledAt = null) else it }) }
                    VisitAction.ATTEND -> {
                        val unskipped = dependencies.record.undo(data, "visit:$id")
                        val item = dependencies.daily(unskipped, LocalDate.parse(visit.date)).find { it.visitId == id }
                        if (item == null) data else dependencies.record(unskipped, item, RecordStatus.DONE, newId(), visit.notes)
                    }
                }
            }
            mutable.update { it.copy(busy = false, error = result.isFailure) }
        }
    }
    fun retry() { mutable.update { it.copy(error = false) }; viewModelScope.launch { dependencies.repository.load() } }
}

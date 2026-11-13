package com.petcare.calendar.presentation.visiteditor

import androidx.lifecycle.viewModelScope
import com.petcare.calendar.R
import com.petcare.calendar.core.di.Dependencies
import com.petcare.calendar.core.navigation.*
import com.petcare.calendar.domain.model.*
import com.petcare.calendar.presentation.app.CalendarViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class VetVisitEditorViewModel(private val dependencies: Dependencies, private val route: Screen.VetVisitEditor) : CalendarViewModel() {
    private val mutable = MutableStateFlow(VetVisitEditorUiState(editing = route.visitId != null))
    val uiState = mutable.asStateFlow()
    private var initialized = false
    init {
        viewModelScope.launch { dependencies.repository.state.collect { store ->
            val pets = store.data.activePets()
            if (!initialized && store.loadState == LoadState.READY) {
                val existing = store.data.visits.find { it.id == route.visitId }
                mutable.update { it.copy(visit = existing ?: it.visit.copy(petId = route.petId?.takeIf { id -> pets.any { pet -> pet.id == id } } ?: pets.firstOrNull()?.id.orEmpty()), pets = pets, loadState = LoadState.READY, notFound = route.visitId != null && existing == null) }
                initialized = true
            } else mutable.update { it.copy(pets = pets, loadState = store.loadState) }
        } }
    }
    private fun edit(transform: (VetAppointment) -> VetAppointment) { mutable.update { it.copy(visit = transform(it.visit), dirty = true, error = null) } }
    fun pet(id: String?) { if (id != null) edit { it.copy(petId = id) } }
    fun reason(value: String) { edit { it.copy(reason = value.take(60)) } }
    fun date(value: String?) { if (value != null) edit { it.copy(date = value) } }
    fun time(value: String) { edit { it.copy(time = value) } }
    fun clinic(value: String) { edit { it.copy(clinic = value.take(80)) } }
    fun notes(value: String) { edit { it.copy(notes = value.take(500)) } }
    fun reminder(value: Boolean) { edit { it.copy(reminder = value) } }
    fun back() { if (uiState.value.busy) return; if (uiState.value.dirty) mutable.update { it.copy(discard = true) } else event(UiEvent.Back) }
    fun keep() { mutable.update { it.copy(discard = false) } }
    fun discard() { event(UiEvent.Back) }
    fun addPet() { event(UiEvent.Navigate(Screen.PetEditor())) }
    fun save() {
        val state = uiState.value
        if (state.busy || state.notFound || state.loadState != LoadState.READY) return
        val visit = state.visit.copy(reason = state.visit.reason.trim())
        val error = when {
            state.pets.none { it.id == visit.petId } -> R.string.pet_required
            visit.reason.isBlank() -> R.string.reason_required
            else -> null
        }
        if (error != null) { mutable.update { it.copy(error = error) }; return }
        mutable.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            val result = dependencies.repository.change { data ->
                val stored = data.visits.find { it.id == visit.id }
                val safeVisit = if (stored != null) visit.copy(status = stored.status, attendedAt = stored.attendedAt, canceledAt = stored.canceledAt) else visit
                data.copy(visits = data.visits.filterNot { it.id == visit.id } + safeVisit)
            }
            mutable.update { it.copy(busy = false, dirty = if (result.isSuccess) false else it.dirty, error = if (result.isFailure) R.string.storage_error else null) }
            if (result.isSuccess) event(UiEvent.Back)
        }
    }
    fun retry() { viewModelScope.launch { dependencies.repository.load() } }
}

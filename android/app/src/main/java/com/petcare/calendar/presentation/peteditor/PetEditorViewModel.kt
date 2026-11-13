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

class PetEditorViewModel(private val dependencies: Dependencies, private val route: Screen.PetEditor) : CalendarViewModel() {
    private val mutable = MutableStateFlow(PetEditorUiState(editing = route.petId != null))
    val uiState = mutable.asStateFlow()
    private var initialized = false
    init {
        viewModelScope.launch { dependencies.repository.state.collect { store ->
            if (!initialized && store.loadState == LoadState.READY) {
                val existing = store.data.pets.find { it.id == route.petId }
                mutable.update { it.copy(pet = existing ?: it.pet, notFound = route.petId != null && existing == null, loadState = LoadState.READY) }
                initialized = true
            } else mutable.update { it.copy(loadState = store.loadState) }
        } }
    }
    private fun edit(transform: (Pet) -> Pet) { mutable.update { it.copy(pet = transform(it.pet), dirty = true, error = null) } }
    fun name(value: String) { edit { it.copy(name = value.take(24)) } }
    fun notes(value: String) { edit { it.copy(notes = value.take(240)) } }
    fun species(value: Species) { edit { it.copy(species = value, photo = value.ordinal) } }
    fun photo(value: Int) { edit { it.copy(photo = value) } }
    fun birthday(value: String?) { edit { it.copy(birthday = value) } }
    fun back() { if (uiState.value.busy) return; if (uiState.value.dirty) mutable.update { it.copy(discard = true) } else event(UiEvent.Back) }
    fun keep() { mutable.update { it.copy(discard = false) } }
    fun discard() { event(UiEvent.Back) }
    fun archive(show: Boolean) { mutable.update { it.copy(archive = show) } }
    fun save() {
        val state = uiState.value
        if (state.busy || state.notFound || state.loadState != LoadState.READY) return
        val pet = state.pet.copy(name = state.pet.name.trim())
        val error = when {
            pet.name.isBlank() -> R.string.name_required
            pet.birthday != null && LocalDate.parse(pet.birthday) > LocalDate.now() -> R.string.birthday_error
            else -> null
        }
        if (error != null) { mutable.update { it.copy(error = error) }; return }
        commit { data -> data.copy(pets = data.pets.filterNot { it.id == pet.id } + pet) }
    }
    fun confirmArchive() {
        val id = uiState.value.pet.id
        mutable.update { it.copy(archive = false) }
        commit { data -> data.copy(pets = data.pets.map { if (it.id == id) it.copy(archived = true) else it }, routines = data.routines.map { if (it.petId == id) it.copy(enabled = false) else it }, visits = data.visits.map { if (it.petId == id && it.status == VisitStatus.PLANNED && LocalDate.parse(it.date) >= LocalDate.now()) it.copy(status = VisitStatus.CANCELED, canceledAt = Instant.now().toString()) else it }) }
    }
    private fun commit(transform: (CalendarData) -> CalendarData) {
        mutable.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            val result = dependencies.repository.change(transform)
            mutable.update { it.copy(busy = false, error = if (result.isFailure) R.string.storage_error else null, dirty = if (result.isSuccess) false else it.dirty) }
            if (result.isSuccess) event(UiEvent.Back)
        }
    }
    fun retry() { viewModelScope.launch { dependencies.repository.load() } }
}

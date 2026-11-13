package com.petcare.calendar.presentation.taskeditor

import androidx.lifecycle.viewModelScope
import com.petcare.calendar.R
import com.petcare.calendar.core.di.Dependencies
import com.petcare.calendar.core.navigation.*
import com.petcare.calendar.domain.model.*
import com.petcare.calendar.presentation.app.CalendarViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class TaskEditorViewModel(private val dependencies: Dependencies, private val route: Screen.TaskEditor) : CalendarViewModel() {
    private val mutable = MutableStateFlow(TaskEditorUiState(editing = route.taskId != null))
    val uiState = mutable.asStateFlow()
    private var initialized = false
    init {
        viewModelScope.launch { dependencies.repository.state.collect { store ->
            val pets = store.data.activePets()
            if (!initialized && store.loadState == LoadState.READY) {
                val existing = store.data.routines.find { it.id == route.taskId }
                mutable.update { it.copy(routine = existing ?: it.routine.copy(petId = route.petId?.takeIf { id -> pets.any { pet -> pet.id == id } } ?: pets.firstOrNull()?.id.orEmpty()), pets = pets, notFound = route.taskId != null && existing == null, loadState = LoadState.READY) }
                initialized = true
            } else mutable.update { it.copy(pets = pets, loadState = store.loadState) }
        } }
    }
    private fun edit(transform: (CareRoutine) -> CareRoutine) { mutable.update { it.copy(routine = transform(it.routine), dirty = true, error = null) } }
    fun pet(id: String?) { if (id != null) edit { it.copy(petId = id) } }
    fun title(value: String) { edit { it.copy(title = value.take(50)) } }
    fun instructions(value: String) { edit { it.copy(instructions = value.take(160)) } }
    fun category(value: Category) { edit { it.copy(category = value) } }
    fun recurrence(value: Recurrence) { edit { it.copy(recurrence = value) } }
    fun weekday(value: Int) { edit { it.copy(weekdays = if (value in it.weekdays) it.weekdays - value else it.weekdays + value) } }
    fun interval(delta: Int) { edit { it.copy(interval = (it.interval + delta).coerceIn(1, 30)) } }
    fun time(id: String, value: String) { edit { it.copy(slots = it.slots.map { slot -> if (slot.id == id) slot.copy(time = value) else slot }) } }
    fun addTime() { edit { routine -> if (routine.slots.size >= 4) routine else routine.copy(slots = routine.slots + TimeSlot(time = LocalTime.parse(routine.slots.last().time).plusHours(4).toString())) } }
    fun removeTime(id: String) { edit { if (it.slots.size <= 1) it else it.copy(slots = it.slots.filterNot { slot -> slot.id == id }) } }
    fun start(value: String?) { if (value != null) edit { it.copy(start = value) } }
    fun end(value: String?) { edit { it.copy(end = value) } }
    fun enabled(value: Boolean) { edit { it.copy(enabled = value) } }
    fun back() { if (uiState.value.busy) return; if (uiState.value.dirty) mutable.update { it.copy(discard = true) } else event(UiEvent.Back) }
    fun keep() { mutable.update { it.copy(discard = false) } }
    fun discard() { event(UiEvent.Back) }
    fun addPet() { event(UiEvent.Navigate(Screen.PetEditor())) }
    fun save() {
        val state = uiState.value
        if (state.busy || state.notFound || state.loadState != LoadState.READY) return
        val routine = state.routine.copy(title = state.routine.title.trim())
        val error = when {
            state.pets.none { it.id == routine.petId } -> R.string.pet_required
            routine.title.isBlank() -> R.string.task_title_required
            routine.recurrence == Recurrence.WEEKDAYS && routine.weekdays.isEmpty() -> R.string.weekday_error
            routine.slots.map { it.time }.distinct().size != routine.slots.size -> R.string.duplicate_times
            routine.end != null && LocalDate.parse(routine.end) < LocalDate.parse(routine.start) -> R.string.date_range_error
            else -> null
        }
        if (error != null) { mutable.update { it.copy(error = error) }; return }
        mutable.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            val result = dependencies.repository.change { it.copy(routines = it.routines.filterNot { row -> row.id == routine.id } + routine) }
            mutable.update { it.copy(busy = false, dirty = if (result.isSuccess) false else it.dirty, error = if (result.isFailure) R.string.storage_error else null) }
            if (result.isSuccess) event(UiEvent.Back)
        }
    }
    fun retry() { viewModelScope.launch { dependencies.repository.load() } }
}

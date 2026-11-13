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

class GameViewModel(private val dependencies: Dependencies, private val route: Screen.Game) : CalendarViewModel() {
    private val mutable = MutableStateFlow(GameUiState(petId = route.petId))
    val uiState = mutable.asStateFlow()
    private var baseline = emptySet<String>()
    private var initialized = false
    private var refreshJob: Job? = null
    private var finishJob: Job? = null
    private var active = false
    init {
        viewModelScope.launch { dependencies.repository.state.collect { refresh() } }
    }

    private fun refresh() {
        val store = dependencies.repository.state.value
        val date = LocalDate.now()
        if (uiState.value.date != date.toString()) {
            initialized = false
            mutable.update { it.copy(date = date.toString(), undoId = null, newDay = true) }
        }
        val pets = store.data.activePets()
        val filter = uiState.value.petId?.takeIf { id -> pets.any { it.id == id } }
        val items = dependencies.daily(store.data, date, filter)
        if (!initialized && store.loadState == LoadState.READY) {
            baseline = dependencies.daily(store.data, date).filter { it.status == CareStatus.DONE }.map { it.id }.toSet()
            initialized = true
        }
        mutable.update { old -> old.copy(loadState = store.loadState, pets = pets, petId = filter, items = items,
            completed = items.count { it.status == CareStatus.DONE }, skipped = items.count { it.status == CareStatus.SKIPPED }, undoId = old.undoId?.takeIf { id -> store.data.logs.any { it.id == id } },
            selectedId = old.selectedId?.takeIf { id -> items.any { it.id == id && it.log == null } } ?: items.firstOrNull { it.log == null }?.id) }
    }

    fun activate() {
        active = true
        refresh()
        if (refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch {
            while (isActive) { delay(60000); refresh() }
        }
    }

    fun pause() { active = false; refreshJob?.cancel(); finishJob?.cancel() }
    fun select(id: String) { mutable.update { it.copy(selectedId = id) } }
    fun filter(id: String?) { mutable.update { it.copy(petId = id, selectedId = null) }; refresh() }
    fun skipDialog(show: Boolean) { mutable.update { it.copy(skipDialog = show, skipNote = "") } }
    fun skipNote(value: String) { mutable.update { it.copy(skipNote = value.take(160)) } }
    fun navigate(screen: Screen) { event(UiEvent.Navigate(screen)) }

    fun record(status: RecordStatus) {
        if (uiState.value.busy) return
        val selected = uiState.value.items.find { it.id == uiState.value.selectedId && it.log == null } ?: return
        mutable.update { it.copy(busy = true, skipDialog = false, error = false, saved = false) }
        val note = uiState.value.skipNote
        viewModelScope.launch {
            val result = dependencies.repository.change { data ->
                val fresh = dependencies.daily(data, LocalDate.parse(selected.date), uiState.value.petId).find { it.id == selected.id && it.log == null }
                if (fresh == null) data else dependencies.record(data, fresh, status, route.sessionId, note)
            }
            mutable.update { it.copy(busy = false, error = result.isFailure, undoId = if (result.isSuccess) selected.id else it.undoId, saved = result.isSuccess) }
            refresh()
            if (result.isSuccess) {
                if (dependencies.repository.state.value.data.settings.haptics) event(UiEvent.Haptic)
                if (uiState.value.items.isNotEmpty() && uiState.value.items.all { it.log != null }) {
                    finishJob?.cancel()
                    finishJob = viewModelScope.launch { delay(500); if (active) finish() }
                }
            }
        }
    }

    fun undo() {
        val id = uiState.value.undoId ?: return
        if (uiState.value.busy) return
        finishJob?.cancel()
        mutable.update { it.copy(busy = true) }
        viewModelScope.launch {
            val result = dependencies.repository.change { dependencies.record.undo(it, id) }
            mutable.update { it.copy(busy = false, error = result.isFailure, undoId = if (result.isSuccess) null else id, saved = false) }
        }
    }

    fun finish() {
        if (uiState.value.busy || uiState.value.loadState != LoadState.READY) return
        mutable.update { it.copy(busy = true) }
        viewModelScope.launch {
            var summaryId: String? = null
            val result = dependencies.repository.change { data ->
                val summary = dependencies.summary(data, route.sessionId, LocalDate.parse(uiState.value.date), uiState.value.petId, baseline, uiState.value.undoId)
                summaryId = summary.id
                data.copy(summaries = (data.summaries + summary).takeLast(30))
            }
            mutable.update { it.copy(busy = false, error = result.isFailure) }
            if (result.isSuccess && active) summaryId?.let { event(UiEvent.Navigate(Screen.GameOver(it))) }
        }
    }
    fun retry() { mutable.update { it.copy(error = false) }; viewModelScope.launch { dependencies.repository.load() } }
}

package com.petcare.calendar.presentation.gameover

import androidx.lifecycle.viewModelScope
import com.petcare.calendar.core.di.Dependencies
import com.petcare.calendar.core.navigation.*
import com.petcare.calendar.domain.model.*
import com.petcare.calendar.presentation.app.CalendarViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GameOverViewModel(private val dependencies: Dependencies, private val route: Screen.GameOver) : CalendarViewModel() {
    private val mutable = MutableStateFlow(GameOverUiState())
    val uiState = mutable.asStateFlow()
    init {
        viewModelScope.launch { dependencies.repository.state.collect { store ->
            val summary = store.data.summaries.find { it.id == route.summaryId }
            mutable.update { it.copy(loadState = store.loadState, summary = summary, newLogs = store.data.logs.filter { log -> log.id in summary?.newLogIds.orEmpty() }, canUndo = summary?.undoId != null && store.data.logs.any { log -> log.id == summary.undoId }) }
        } }
    }
    fun replay() { event(UiEvent.Navigate(Screen.Game(petId = uiState.value.summary?.petId))) }
    fun menu() { event(UiEvent.Menu) }
    fun back() { event(UiEvent.Back) }
    fun undo() {
        val id = uiState.value.summary?.undoId ?: return
        if (uiState.value.busy) return
        mutable.update { it.copy(busy = true) }
        viewModelScope.launch {
            val result = dependencies.repository.change { data ->
                val restored = dependencies.record.undo(data, id)
                restored.copy(summaries = restored.summaries.filterNot { it.id == route.summaryId })
            }
            mutable.update { it.copy(busy = false, error = result.isFailure) }
            if (result.isSuccess) event(UiEvent.Back)
        }
    }
    fun retry() { viewModelScope.launch { dependencies.repository.load() }; mutable.update { it.copy(error = false) } }
}

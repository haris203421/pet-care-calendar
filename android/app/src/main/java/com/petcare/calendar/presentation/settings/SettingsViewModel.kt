package com.petcare.calendar.presentation.settings

import androidx.lifecycle.viewModelScope
import com.petcare.calendar.core.di.Dependencies
import com.petcare.calendar.core.navigation.*
import com.petcare.calendar.domain.model.*
import com.petcare.calendar.presentation.app.CalendarViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class DataAction { RESET, SAMPLES, CONVERT }
class SettingsViewModel(private val dependencies: Dependencies) : CalendarViewModel() {
    private val mutable = MutableStateFlow(SettingsUiState())
    val uiState = mutable.asStateFlow()
    init {
        viewModelScope.launch { dependencies.repository.state.collect { store -> mutable.update { it.copy(loadState = store.loadState, settings = store.data.settings, archived = store.data.pets.filter { pet -> pet.archived }, empty = store.data.pets.isEmpty() && store.data.logs.isEmpty()) } } }
        viewModelScope.launch { dependencies.platform.permission.collect { value -> mutable.update { it.copy(permission = value) } } }
        viewModelScope.launch {
            dependencies.platform.permissionResult.drop(1).collect {
                if (uiState.value.permissionPending) {
                    val allowed = dependencies.platform.permission.value
                    mutable.update { it.copy(permissionPending = false, permissionDenied = !allowed) }
                    if (allowed) saveSettings { it.copy(reminders = true, reminderSince = System.currentTimeMillis()) }
                }
            }
        }
    }
    fun reminders(value: Boolean) {
        if (!value) saveSettings { it.copy(reminders = false) }
        else if (dependencies.platform.permissionGranted()) saveSettings { it.copy(reminders = true, reminderSince = System.currentTimeMillis()) }
        else { mutable.update { it.copy(permissionPending = true) }; event(UiEvent.Permission) }
    }
    fun settings() { event(UiEvent.AppSettings) }
    fun routineLead(value: Int) { saveSettings { it.copy(routineLead = value) } }
    fun visitLead(value: Int) { saveSettings { it.copy(visitLead = value) } }
    fun timeFormat(value: TimeFormat) { saveSettings { it.copy(timeFormat = value) } }
    fun haptics(value: Boolean) { saveSettings { it.copy(haptics = value) } }
    fun motion(value: Boolean) { saveSettings { it.copy(reducedMotion = value) } }
    private fun saveSettings(transform: (AppSettings) -> AppSettings) { change { data -> data.copy(settings = transform(data.settings)) } }
    fun restore(id: String) { change { data -> data.copy(pets = data.pets.map { if (it.id == id) it.copy(archived = false) else it }) } }
    fun ask(action: DataAction?) { mutable.update { it.copy(pending = action) } }
    fun confirm() {
        val action = uiState.value.pending ?: return
        mutable.update { it.copy(pending = null, busy = true) }
        viewModelScope.launch {
            val result = when (action) {
                DataAction.RESET -> dependencies.repository.reset()
                DataAction.SAMPLES -> dependencies.repository.loadSamples()
                DataAction.CONVERT -> dependencies.repository.change { it.copy(settings = it.settings.copy(sampleMode = false)) }
            }
            mutable.update { it.copy(busy = false, error = result.isFailure, saved = result.isSuccess) }
            if (result.isSuccess && action == DataAction.RESET) event(UiEvent.Menu)
        }
    }
    private fun change(transform: (CalendarData) -> CalendarData) {
        if (uiState.value.busy) return
        mutable.update { it.copy(busy = true, saved = false) }
        viewModelScope.launch { val result = dependencies.repository.change(transform); mutable.update { it.copy(busy = false, error = result.isFailure, saved = result.isSuccess) } }
    }
    fun back() { event(UiEvent.Back) }
    fun menu() { event(UiEvent.Menu) }
    fun retry() { mutable.update { it.copy(error = false) }; viewModelScope.launch { dependencies.repository.load() } }
}

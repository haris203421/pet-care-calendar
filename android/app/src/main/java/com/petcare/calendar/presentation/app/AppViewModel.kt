package com.petcare.calendar.presentation.app

import androidx.lifecycle.viewModelScope
import com.petcare.calendar.core.di.Dependencies
import com.petcare.calendar.core.navigation.Screen
import com.petcare.calendar.core.navigation.UiEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class AppViewModel(private val dependencies: Dependencies) : CalendarViewModel() {
    private val mutableScreen = MutableStateFlow<Screen>(Screen.Splash)
    val screen = mutableScreen.asStateFlow()
    private val mutable = MutableStateFlow(AppUiState())
    val uiState = mutable.asStateFlow()
    private val backStack = mutableListOf<Screen>()

    init {
        viewModelScope.launch { dependencies.repository.load() }
        viewModelScope.launch { dependencies.repository.state.collect { store -> mutable.update { it.copy(settings = store.data.settings) } } }
    }

    fun splashFinished() {
        if (screen.value == Screen.Splash) mutableScreen.value = Screen.Menu
    }

    fun navigate(next: Screen) {
        if (next == Screen.Menu) { menu(); return }
        if (next == screen.value) return
        val current = screen.value
        if (sameDestination(current, next)) return
        val existing = backStack.indexOfLast { sameDestination(it, next) }
        if (existing >= 0) {
            mutableScreen.value = if (current is Screen.GameOver && next is Screen.Game) next else backStack[existing]
            while (backStack.size > existing) backStack.removeAt(backStack.lastIndex)
        } else {
            if (current != Screen.Splash) backStack += current
            mutableScreen.value = next
        }
    }

    private fun sameDestination(a: Screen, b: Screen): Boolean = when {
        a is Screen.Game && b is Screen.Game -> true
        a == Screen.Schedule && b == Screen.Schedule -> true
        a == Screen.VetVisits && b == Screen.VetVisits -> true
        else -> false
    }

    fun back() {
        if (backStack.isNotEmpty()) mutableScreen.value = backStack.removeAt(backStack.lastIndex)
    }

    fun menu() { backStack.clear(); mutableScreen.value = Screen.Menu }
    fun refresh() { viewModelScope.launch { dependencies.repository.load() }; dependencies.platform.refreshPermission() }
    fun handle(event: UiEvent) {
        when (event) {
            is UiEvent.Navigate -> navigate(event.screen)
            UiEvent.Back -> back()
            UiEvent.Menu -> menu()
            UiEvent.Permission -> dependencies.platform.requestNotifications?.invoke()
            UiEvent.AppSettings -> dependencies.platform.openSettings?.invoke()
            UiEvent.Haptic -> Unit
        }
    }
}

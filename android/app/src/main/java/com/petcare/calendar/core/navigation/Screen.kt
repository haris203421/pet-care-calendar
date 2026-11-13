package com.petcare.calendar.core.navigation

import com.petcare.calendar.domain.model.newId

sealed interface Screen {
    data object Splash : Screen
    data object Menu : Screen
    data class Game(val sessionId: String = newId(), val petId: String? = null) : Screen
    data class GameOver(val summaryId: String) : Screen
    data object Schedule : Screen
    data object VetVisits : Screen
    data class PetHistory(val petId: String? = null) : Screen
    data object Settings : Screen
    data class PetEditor(val petId: String? = null, val draftId: String = newId()) : Screen
    data class TaskEditor(val taskId: String? = null, val petId: String? = null, val draftId: String = newId()) : Screen
    data class VetVisitEditor(val visitId: String? = null, val petId: String? = null, val draftId: String = newId()) : Screen
}

sealed interface UiEvent {
    data class Navigate(val screen: Screen) : UiEvent
    data object Back : UiEvent
    data object Menu : UiEvent
    data object Permission : UiEvent
    data object AppSettings : UiEvent
    data object Haptic : UiEvent
}

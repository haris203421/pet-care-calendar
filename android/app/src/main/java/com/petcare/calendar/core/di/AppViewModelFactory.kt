package com.petcare.calendar.core.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.petcare.calendar.core.navigation.Screen
import com.petcare.calendar.presentation.app.AppViewModel
import com.petcare.calendar.presentation.splash.SplashViewModel
import com.petcare.calendar.presentation.menu.MenuViewModel
import com.petcare.calendar.presentation.game.GameViewModel
import com.petcare.calendar.presentation.gameover.GameOverViewModel
import com.petcare.calendar.presentation.schedule.ScheduleViewModel
import com.petcare.calendar.presentation.vetvisits.VetVisitsViewModel
import com.petcare.calendar.presentation.history.PetHistoryViewModel
import com.petcare.calendar.presentation.settings.SettingsViewModel
import com.petcare.calendar.presentation.peteditor.PetEditorViewModel
import com.petcare.calendar.presentation.taskeditor.TaskEditorViewModel
import com.petcare.calendar.presentation.visiteditor.VetVisitEditorViewModel

class AppViewModelFactory(private val screen: Screen = Screen.Splash) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val services = ServiceLocator.services
        val instance: ViewModel = when (modelClass) {
            AppViewModel::class.java -> AppViewModel(services)
            SplashViewModel::class.java -> SplashViewModel(services.platform)
            MenuViewModel::class.java -> MenuViewModel(services)
            GameViewModel::class.java -> GameViewModel(services, screen as Screen.Game)
            GameOverViewModel::class.java -> GameOverViewModel(services, screen as Screen.GameOver)
            ScheduleViewModel::class.java -> ScheduleViewModel(services)
            VetVisitsViewModel::class.java -> VetVisitsViewModel(services)
            PetHistoryViewModel::class.java -> PetHistoryViewModel(services, screen as Screen.PetHistory)
            SettingsViewModel::class.java -> SettingsViewModel(services)
            PetEditorViewModel::class.java -> PetEditorViewModel(services, screen as Screen.PetEditor)
            TaskEditorViewModel::class.java -> TaskEditorViewModel(services, screen as Screen.TaskEditor)
            VetVisitEditorViewModel::class.java -> VetVisitEditorViewModel(services, screen as Screen.VetVisitEditor)
            else -> error(modelClass.name)
        }
        return instance as T
    }
}

package com.petcare.calendar.presentation.app

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.view.WindowCompat
import com.petcare.calendar.core.di.AppViewModelFactory
import com.petcare.calendar.core.navigation.*
import com.petcare.calendar.core.ui.LocalTimeFormat
import com.petcare.calendar.presentation.splash.*
import com.petcare.calendar.presentation.menu.*
import com.petcare.calendar.presentation.game.*
import com.petcare.calendar.presentation.gameover.*
import com.petcare.calendar.presentation.schedule.*
import com.petcare.calendar.presentation.vetvisits.*
import com.petcare.calendar.presentation.history.*
import com.petcare.calendar.presentation.settings.*
import com.petcare.calendar.presentation.peteditor.*
import com.petcare.calendar.presentation.taskeditor.*
import com.petcare.calendar.presentation.visiteditor.*

@Composable
fun AppRoot() {
    val app: AppViewModel = viewModel(factory = AppViewModelFactory())
    val screen by app.screen.collectAsStateWithLifecycle()
    val state by app.uiState.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val haptic = LocalHapticFeedback.current
    val onEvent: (UiEvent) -> Unit = { event ->
        if (event == UiEvent.Haptic) haptic.performHapticFeedback(HapticFeedbackType.Confirm) else app.handle(event)
    }
    DisposableEffect(activity, screen is Screen.Splash) {
        activity?.window?.let { window ->
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightStatusBars = screen != Screen.Splash
                isAppearanceLightNavigationBars = screen != Screen.Splash
            }
        }
        onDispose { Unit }
    }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) app.refresh() }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    BackHandler {
        if (screen == Screen.Menu || screen == Screen.Splash) activity?.finish() else app.back()
    }
    CompositionLocalProvider(LocalTimeFormat provides state.settings.timeFormat) {
        AnimatedContent(targetState = screen, transitionSpec = {
            val duration = if (state.settings.reducedMotion) 0 else if (initialState == Screen.Splash) 450 else 220
            fadeIn(tween(duration)) togetherWith fadeOut(tween(duration))
        }, label = "screen_transition") { route ->
            val onRouteEvent: (UiEvent) -> Unit = { event -> if (app.screen.value == route) onEvent(event) }
            when (route) {
                Screen.Splash -> {
                    val vm: SplashViewModel = viewModel(factory = AppViewModelFactory())
                    SplashRoute(vm, app::splashFinished)
                }
                Screen.Menu -> {
                    val vm: MenuViewModel = viewModel(factory = AppViewModelFactory())
                    val ui by vm.uiState.collectAsStateWithLifecycle()
                    ObserveEvents(vm, onRouteEvent)
                    MenuScreen(ui, vm::select, vm::play, vm::navigate, vm::sampleChoice, vm::keepSamples, vm::startEmpty, vm::retry, vm::openReminder)
                }
                is Screen.Game -> {
                    val vm: GameViewModel = viewModel(key = route.sessionId, factory = AppViewModelFactory(route))
                    val ui by vm.uiState.collectAsStateWithLifecycle()
                    ObserveEvents(vm, onRouteEvent)
                    DisposableEffect(vm, lifecycle) {
                        vm.activate()
                        val observer = LifecycleEventObserver { _, event ->
                            when (event) { Lifecycle.Event.ON_START -> vm.activate(); Lifecycle.Event.ON_STOP -> vm.pause(); else -> Unit }
                        }
                        lifecycle.addObserver(observer)
                        onDispose { vm.pause(); lifecycle.removeObserver(observer) }
                    }
                    GameScreen(ui, app::back, vm::navigate, vm::filter, vm::select, vm::record, vm::skipDialog, vm::skipNote, vm::finish, vm::undo, vm::retry)
                }
                is Screen.GameOver -> {
                    val vm: GameOverViewModel = viewModel(key = route.summaryId, factory = AppViewModelFactory(route))
                    val ui by vm.uiState.collectAsStateWithLifecycle()
                    ObserveEvents(vm, onRouteEvent)
                    GameOverScreen(ui, vm::back, vm::replay, vm::menu, vm::undo, vm::retry)
                }
                Screen.Schedule -> {
                    val vm: ScheduleViewModel = viewModel(factory = AppViewModelFactory())
                    val ui by vm.uiState.collectAsStateWithLifecycle()
                    ObserveEvents(vm, onRouteEvent)
                    ScheduleScreen(ui, vm::back, vm::navigate, vm::filterPet, vm::filter, vm::toggle, vm::archive, vm::confirmArchive, vm::retry)
                }
                Screen.VetVisits -> {
                    val vm: VetVisitsViewModel = viewModel(factory = AppViewModelFactory())
                    val ui by vm.uiState.collectAsStateWithLifecycle()
                    ObserveEvents(vm, onRouteEvent)
                    VetVisitsScreen(ui, vm::back, vm::navigate, vm::pet, vm::tab, vm::expand, vm::ask, vm::confirm, vm::dismiss, vm::retry)
                }
                is Screen.PetHistory -> {
                    val vm: PetHistoryViewModel = viewModel(key = "history_${route.petId}", factory = AppViewModelFactory(route))
                    val ui by vm.uiState.collectAsStateWithLifecycle()
                    ObserveEvents(vm, onRouteEvent)
                    PetHistoryScreen(ui, vm::back, vm::navigate, vm::pet, vm::range, vm::kind, vm::clearFilters, vm::expand, vm::retry)
                }
                Screen.Settings -> {
                    val vm: SettingsViewModel = viewModel(factory = AppViewModelFactory())
                    val ui by vm.uiState.collectAsStateWithLifecycle()
                    ObserveEvents(vm, onRouteEvent)
                    SettingsScreen(ui, vm::back, vm::menu, vm::reminders, vm::settings, vm::routineLead, vm::visitLead, vm::timeFormat, vm::haptics, vm::motion, vm::restore, vm::ask, vm::confirm, vm::retry)
                }
                is Screen.PetEditor -> {
                    val vm: PetEditorViewModel = viewModel(key = route.draftId, factory = AppViewModelFactory(route))
                    val ui by vm.uiState.collectAsStateWithLifecycle()
                    ObserveEvents(vm, onRouteEvent)
                    PetEditorScreen(ui, vm::back, vm::name, vm::species, vm::photo, vm::birthday, vm::notes, vm::save, vm::archive, vm::confirmArchive, vm::discard, vm::keep, vm::retry)
                }
                is Screen.TaskEditor -> {
                    val vm: TaskEditorViewModel = viewModel(key = route.draftId, factory = AppViewModelFactory(route))
                    val ui by vm.uiState.collectAsStateWithLifecycle()
                    ObserveEvents(vm, onRouteEvent)
                    TaskEditorScreen(ui, vm::back, vm::pet, vm::title, vm::category, vm::instructions, vm::recurrence, vm::weekday, vm::interval, vm::time, vm::addTime, vm::removeTime, vm::start, vm::end, vm::enabled, vm::save, vm::discard, vm::keep, vm::addPet, vm::retry)
                }
                is Screen.VetVisitEditor -> {
                    val vm: VetVisitEditorViewModel = viewModel(key = route.draftId, factory = AppViewModelFactory(route))
                    val ui by vm.uiState.collectAsStateWithLifecycle()
                    ObserveEvents(vm, onRouteEvent)
                    VetVisitEditorScreen(ui, vm::back, vm::pet, vm::reason, vm::date, vm::time, vm::clinic, vm::notes, vm::reminder, vm::save, vm::discard, vm::keep, vm::addPet, vm::retry)
                }
            }
        }
    }
}

@Composable
private fun ObserveEvents(viewModel: CalendarViewModel, onEvent: (UiEvent) -> Unit) {
    val currentHandler by rememberUpdatedState(onEvent)
    LaunchedEffect(Unit) { viewModel.events.collect { currentHandler(it) } }
}

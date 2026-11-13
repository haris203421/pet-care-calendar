package com.petcare.calendar.presentation.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petcare.calendar.core.navigation.UiEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class CalendarViewModel : ViewModel() {
    private val eventChannel = Channel<UiEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()
    protected fun event(value: UiEvent) { viewModelScope.launch { eventChannel.send(value) } }
}

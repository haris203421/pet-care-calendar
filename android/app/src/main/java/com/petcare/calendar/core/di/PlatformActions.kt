package com.petcare.calendar.core.di

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlatformActions {
    var requestNotifications: (() -> Unit)? = null
    var openSettings: (() -> Unit)? = null
    var permissionGranted: () -> Boolean = { false }
    var systemReducedMotion: () -> Boolean = { false }
    private val mutablePermission = MutableStateFlow(false)
    val permission = mutablePermission.asStateFlow()
    private val mutableResult = MutableStateFlow(0)
    val permissionResult = mutableResult.asStateFlow()
    private val mutableDestination = MutableStateFlow<String?>(null)
    val destination = mutableDestination.asStateFlow()
    fun refreshPermission() { mutablePermission.value = permissionGranted() }
    fun reportPermissionResult() { refreshPermission(); mutableResult.value += 1 }
    fun setDestination(value: String?) { mutableDestination.value = value }
}

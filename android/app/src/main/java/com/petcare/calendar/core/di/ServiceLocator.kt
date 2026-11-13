package com.petcare.calendar.core.di

import android.content.Context
import android.provider.Settings
import com.petcare.calendar.data.local.LocalDataSerializer
import com.petcare.calendar.data.local.LocalPreferencesStore
import com.petcare.calendar.data.reminder.LocalReminderScheduler
import com.petcare.calendar.data.repository.PetCareRepositoryImpl
import com.petcare.calendar.domain.repository.PetCareRepository
import com.petcare.calendar.domain.usecase.*

object ServiceLocator {
    private var dependencies: Dependencies? = null
    val services: Dependencies get() = checkNotNull(dependencies)
    fun initialize(context: Context) {
        if (dependencies != null) return
        val appContext = context.applicationContext
        val store = LocalPreferencesStore(appContext, LocalDataSerializer())
        val reminders = LocalReminderScheduler(appContext, store)
        val platform = PlatformActions().apply {
            permissionGranted = reminders::allowed
            systemReducedMotion = { Settings.Global.getFloat(appContext.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f }
            refreshPermission()
        }
        val daily = BuildDailyCareUseCase()
        dependencies = Dependencies(PetCareRepositoryImpl(store, { reminders.reconcile(it) }), daily, RecordCareUseCase(), BuildCareSummaryUseCase(daily), platform)
    }
}

data class Dependencies(
    val repository: PetCareRepository, val daily: BuildDailyCareUseCase,
    val record: RecordCareUseCase, val summary: BuildCareSummaryUseCase,
    val platform: PlatformActions
)

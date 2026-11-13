package com.petcare.calendar.domain.repository

import com.petcare.calendar.domain.model.CalendarData
import com.petcare.calendar.domain.model.CalendarStore
import kotlinx.coroutines.flow.StateFlow

interface PetCareRepository {
    val state: StateFlow<CalendarStore>
    suspend fun load()
    suspend fun change(transform: (CalendarData) -> CalendarData): Result<Unit>
    suspend fun reset(): Result<Unit>
    suspend fun loadSamples(): Result<Unit>
}

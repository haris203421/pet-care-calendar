package com.petcare.calendar.domain.usecase

import com.petcare.calendar.domain.model.*
import java.time.LocalDate

class BuildCareSummaryUseCase(private val daily: BuildDailyCareUseCase) {
    operator fun invoke(data: CalendarData, sessionId: String, date: LocalDate, petId: String?, baseline: Set<String>, undoId: String?): CareSessionSummary {
        val items = daily(data, date, petId)
        val completed = items.count { it.status == CareStatus.DONE }
        val skipped = items.count { it.status == CareStatus.SKIPPED }
        return CareSessionSummary(sessionId = sessionId, date = date.toString(), petId = petId, completed = completed, skipped = skipped,
            remaining = items.size - completed - skipped, newLogIds = items.filter { it.status == CareStatus.DONE && it.id !in baseline }.map { it.id },
            undoId = undoId, baseline = baseline)
    }
}

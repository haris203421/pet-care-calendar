package com.petcare.calendar.domain.usecase

import com.petcare.calendar.domain.model.*
import java.time.Instant
import java.time.ZoneId

class RecordCareUseCase {
    operator fun invoke(data: CalendarData, item: CareOccurrence, status: RecordStatus, sessionId: String, note: String = ""): CalendarData {
        if (data.logs.any { it.id == item.id }) return data
        val instant = Instant.now()
        val zone = ZoneId.systemDefault()
        val log = CareLog(item.id, item.petId, item.petName, item.species, item.photo, item.title, item.date, item.time, status, instant.toString(), instant.atZone(zone).toLocalDateTime().toString(), zone.id, note.ifBlank { item.instructions }, sessionId, item.visitId)
        return data.copy(logs = data.logs + log, visits = data.visits.map {
            if (it.id == item.visitId && status == RecordStatus.DONE) it.copy(status = VisitStatus.VISITED, attendedAt = instant.toString()) else it
        })
    }

    fun undo(data: CalendarData, id: String): CalendarData {
        val log = data.logs.find { it.id == id } ?: return data
        return data.copy(logs = data.logs.filterNot { it.id == id }, visits = data.visits.map {
            if (it.id == log.visitId) it.copy(status = VisitStatus.PLANNED, attendedAt = null) else it
        })
    }
}

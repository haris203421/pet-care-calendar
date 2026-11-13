package com.petcare.calendar.domain.usecase

import com.petcare.calendar.domain.model.*
import java.time.LocalDate
import java.time.LocalDateTime

class BuildDailyCareUseCase {
    operator fun invoke(data: CalendarData, date: LocalDate, petId: String? = null, now: LocalDateTime = LocalDateTime.now()): List<CareOccurrence> {
        val pets = data.activePets().associateBy { it.id }
        val logs = data.logs.associateBy { it.id }
        val items = mutableListOf<CareOccurrence>()
        data.routines.filter { it.occursOn(date) }.forEach { routine ->
            val pet = pets[routine.petId] ?: return@forEach
            routine.slots.forEach { slot ->
                val id = "${routine.id}:$date:${slot.id}"
                val scheduled = date.atTime(java.time.LocalTime.parse(slot.time))
                val log = logs[id]
                val status = when (log?.status) {
                    RecordStatus.DONE -> CareStatus.DONE
                    RecordStatus.SKIPPED -> CareStatus.SKIPPED
                    null -> when {
                        now < scheduled -> CareStatus.UPCOMING
                        now > scheduled.plusMinutes(30) -> CareStatus.OVERDUE
                        else -> CareStatus.DUE
                    }
                }
                items += CareOccurrence(id, pet.id, pet.name, pet.photo, pet.species, routine.title, routine.instructions, date.toString(), slot.time, status, log = log)
            }
        }
        data.visits.filter { it.date == date.toString() && it.status != VisitStatus.CANCELED }.forEach { visit ->
            val pet = pets[visit.petId] ?: return@forEach
            val id = "visit:${visit.id}"
            val log = logs[id]
            val scheduled = date.atTime(java.time.LocalTime.parse(visit.time))
            val status = when (log?.status) {
                RecordStatus.DONE -> CareStatus.DONE
                RecordStatus.SKIPPED -> CareStatus.SKIPPED
                null -> if (now < scheduled) CareStatus.UPCOMING else CareStatus.OVERDUE
            }
            items += CareOccurrence(id, pet.id, pet.name, pet.photo, pet.species, visit.reason, visit.clinic, visit.date, visit.time, status, visit.id, log)
        }
        val ids = items.map { it.id }.toSet()
        data.logs.filter { it.date == date.toString() && it.id !in ids }.forEach { log ->
            items += CareOccurrence(log.id, log.petId, log.petName, log.photo, log.species, log.title, log.note, log.date, log.time, if (log.status == RecordStatus.DONE) CareStatus.DONE else CareStatus.SKIPPED, log.visitId, log)
        }
        return items.map { item ->
            val log = item.log
            if (log == null) item else item.copy(petName = log.petName, photo = log.photo, species = log.species, title = log.title, instructions = log.note, date = log.date, time = log.time)
        }.filter { petId == null || it.petId == petId }
            .sortedWith(compareBy<CareOccurrence> { it.log != null }.thenBy { it.time }.thenBy { it.petName }.thenBy { it.id })
    }
}

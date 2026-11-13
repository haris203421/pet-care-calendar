package com.petcare.calendar.domain.usecase

import com.petcare.calendar.domain.model.*
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class CalculateNextReminderUseCase {
    operator fun invoke(data: CalendarData, now: Long = System.currentTimeMillis(), includeDue: Boolean = false): List<ReminderItem> {
        if (!data.settings.reminders || data.settings.sampleMode) return emptyList()
        val zone = ZoneId.systemDefault()
        val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        val pets = data.activePets().associateBy { it.id }
        val recorded = data.logs.map { it.id }.toSet()
        val result = mutableListOf<ReminderItem>()
        for (offset in 0L..31L) {
            val date = today.plusDays(offset)
            data.routines.filter { it.occursOn(date) }.forEach { routine ->
                val pet = pets[routine.petId] ?: return@forEach
                routine.slots.forEach { slot ->
                    val id = "${routine.id}:$date:${slot.id}"
                    val at = date.atTime(LocalTime.parse(slot.time)).atZone(zone).toInstant().toEpochMilli() - data.settings.routineLead * 60000L
                    if (id !in recorded && id !in data.delivered && at >= data.settings.reminderSince && (includeDue || at > now)) result += ReminderItem(id, pet.name, routine.title, slot.time + " · " + routine.instructions, at)
                }
            }
        }
        data.visits.filter { it.status == VisitStatus.PLANNED && it.reminder }.forEach { visit ->
            val pet = pets[visit.petId] ?: return@forEach
            val id = "visit:${visit.id}"
            val time = LocalDate.parse(visit.date).atTime(LocalTime.parse(visit.time)).atZone(zone).toInstant().toEpochMilli()
            val preferred = time - data.settings.visitLead * 60000L
            val at = if (preferred < data.settings.reminderSince) time else preferred
            if (id !in recorded && id !in data.delivered && time >= now && at >= data.settings.reminderSince && (includeDue || at > now)) result += ReminderItem(id, pet.name, visit.reason, visit.time + " · " + visit.clinic, at)
        }
        return result.sortedBy { it.at }
    }
}

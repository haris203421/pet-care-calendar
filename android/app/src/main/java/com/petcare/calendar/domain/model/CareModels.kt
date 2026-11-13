package com.petcare.calendar.domain.model

import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

fun newId(): String = UUID.randomUUID().toString()

enum class Species { DOG, CAT, RABBIT, BIRD }
enum class Recurrence { DAILY, WEEKDAYS, INTERVAL }
enum class Category { FEEDING, GROOMING, CUSTOM }
enum class RecordStatus { DONE, SKIPPED }
enum class CareStatus { UPCOMING, DUE, OVERDUE, DONE, SKIPPED }
enum class VisitStatus { PLANNED, VISITED, CANCELED }
enum class LoadState { LOADING, READY, ERROR }
enum class TimeFormat { SYSTEM, H12, H24 }

data class Pet(
    val id: String = newId(), val name: String, val species: Species = Species.DOG,
    val photo: Int = 0, val birthday: String? = null, val notes: String = "",
    val archived: Boolean = false, val createdAt: String = java.time.Instant.now().toString()
)

data class TimeSlot(val id: String = newId(), val time: String = "08:00")

data class CareRoutine(
    val id: String = newId(), val petId: String, val title: String,
    val category: Category = Category.CUSTOM, val instructions: String = "",
    val recurrence: Recurrence = Recurrence.DAILY, val weekdays: Set<Int> = setOf(1, 2, 3, 4, 5),
    val interval: Int = 1, val slots: List<TimeSlot> = listOf(TimeSlot()),
    val start: String = LocalDate.now().toString(), val end: String? = null,
    val enabled: Boolean = true, val archived: Boolean = false
)

data class VetAppointment(
    val id: String = newId(), val petId: String, val reason: String,
    val date: String = LocalDate.now().plusDays(1).toString(), val time: String = "10:00",
    val clinic: String = "", val notes: String = "", val reminder: Boolean = true,
    val status: VisitStatus = VisitStatus.PLANNED, val attendedAt: String? = null,
    val canceledAt: String? = null
)

data class CareLog(
    val id: String, val petId: String, val petName: String, val species: Species,
    val photo: Int, val title: String, val date: String, val time: String,
    val status: RecordStatus, val recordedAt: String, val recordedLocal: String,
    val zone: String, val note: String = "", val sessionId: String,
    val visitId: String? = null
)

data class CareOccurrence(
    val id: String, val petId: String, val petName: String, val photo: Int,
    val species: Species, val title: String, val instructions: String,
    val date: String, val time: String, val status: CareStatus,
    val visitId: String? = null, val log: CareLog? = null
)

data class CareSessionSummary(
    val id: String = newId(), val sessionId: String, val date: String,
    val petId: String?, val completed: Int, val skipped: Int, val remaining: Int,
    val newLogIds: List<String>, val undoId: String?, val baseline: Set<String>,
    val createdAt: String = java.time.Instant.now().toString()
)

data class AppSettings(
    val reminders: Boolean = false, val routineLead: Int = 0, val visitLead: Int = 1440,
    val timeFormat: TimeFormat = TimeFormat.SYSTEM, val haptics: Boolean = true,
    val reducedMotion: Boolean = false, val sampleMode: Boolean = true,
    val reminderSince: Long = 0L
)

data class CalendarData(
    val pets: List<Pet> = emptyList(), val routines: List<CareRoutine> = emptyList(),
    val visits: List<VetAppointment> = emptyList(), val logs: List<CareLog> = emptyList(),
    val summaries: List<CareSessionSummary> = emptyList(), val settings: AppSettings = AppSettings(),
    val delivered: Set<String> = emptySet()
)

data class CalendarStore(
    val data: CalendarData = CalendarData(), val loadState: LoadState = LoadState.LOADING
)

data class ReminderItem(val id: String, val petName: String, val title: String, val detail: String, val at: Long)

fun CalendarData.activePets(): List<Pet> = pets.filterNot { it.archived }

fun CareRoutine.occursOn(date: LocalDate): Boolean {
    val first = LocalDate.parse(start)
    if (!enabled || archived || date < first || (end != null && date > LocalDate.parse(end))) return false
    return when (recurrence) {
        Recurrence.DAILY -> true
        Recurrence.WEEKDAYS -> date.dayOfWeek.value in weekdays
        Recurrence.INTERVAL -> java.time.temporal.ChronoUnit.DAYS.between(first, date) % interval.coerceAtLeast(1) == 0L
    }
}

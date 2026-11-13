package com.petcare.calendar.data.sample

import com.petcare.calendar.domain.model.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

object SampleData {
    fun create(today: LocalDate = LocalDate.now()): CalendarData {
        val milo = Pet(id = "sample-milo", name = "Milo", birthday = "2022-04-18", notes = "Loves a calm breakfast and a short brush.")
        val luna = Pet(id = "sample-luna", name = "Luna", species = Species.CAT, photo = 1, birthday = "2021-09-03", notes = "Prefers grooming on the sofa.")
        val pippin = Pet(id = "sample-pippin", name = "Pippin", species = Species.RABBIT, photo = 2, birthday = "2023-02-11", notes = "Keep the hay rack topped up.")
        val start = today.minusDays(30).toString()
        val routines = listOf(
            CareRoutine(id = "milo-breakfast", petId = milo.id, title = "Breakfast", category = Category.FEEDING, instructions = "One level scoop and fresh water", slots = listOf(TimeSlot("breakfast", "08:00")), start = start),
            CareRoutine(id = "milo-evening", petId = milo.id, title = "Evening meal", category = Category.FEEDING, instructions = "Use the blue bowl", slots = listOf(TimeSlot("evening", "18:00")), start = start),
            CareRoutine(id = "luna-water", petId = luna.id, title = "Fresh water", category = Category.FEEDING, slots = listOf(TimeSlot("water", "08:30")), start = start),
            CareRoutine(id = "luna-brush", petId = luna.id, title = "Brush coat", category = Category.GROOMING, instructions = "Use the soft brush", recurrence = Recurrence.WEEKDAYS, weekdays = setOf(2, 5), slots = listOf(TimeSlot("brush", "18:30")), start = start),
            CareRoutine(id = "pippin-hay", petId = pippin.id, title = "Hay check", category = Category.FEEDING, slots = listOf(TimeSlot("hay", "09:00")), start = start),
            CareRoutine(id = "pippin-bedding", petId = pippin.id, title = "Refresh bedding", recurrence = Recurrence.INTERVAL, interval = 3, slots = listOf(TimeSlot("bedding", "17:00")), start = today.toString())
        )
        val visits = listOf(
            VetAppointment(id = "milo-wellness", petId = milo.id, reason = "Annual wellness visit", date = today.plusDays(7).toString(), clinic = "Maple Veterinary Clinic", notes = "Bring the vaccination record"),
            VetAppointment(id = "luna-checkup", petId = luna.id, reason = "Routine check-up", date = today.minusDays(10).toString(), time = "11:00", clinic = "Maple Veterinary Clinic", notes = "Owner notes: discussed feeding routine", status = VisitStatus.VISITED, attendedAt = today.minusDays(10).atTime(11, 30).atZone(ZoneId.systemDefault()).toInstant().toString())
        )
        fun history(pet: Pet, title: String, date: LocalDate, time: String, id: String, recorded: String): CareLog {
            val local = date.atTime(LocalTime.parse(recorded))
            return CareLog(id, pet.id, pet.name, pet.species, pet.photo, title, date.toString(), time, RecordStatus.DONE, local.atZone(ZoneId.systemDefault()).toInstant().toString(), local.toString(), ZoneId.systemDefault().id, sessionId = "sample-history")
        }
        val yesterday = today.minusDays(1)
        val logs = listOf(
            history(milo, "Breakfast", yesterday, "08:00", "milo-breakfast:$yesterday:breakfast", "08:05"),
            history(luna, "Fresh water", yesterday, "08:30", "luna-water:$yesterday:water", "08:40"),
            history(pippin, "Hay check", today.minusDays(2), "09:00", "pippin-hay:${today.minusDays(2)}:hay", "09:10"),
            history(luna, visits[1].reason, today.minusDays(10), "11:00", "visit:luna-checkup", "11:30").copy(visitId = "luna-checkup", note = visits[1].notes)
        )
        return CalendarData(listOf(milo, luna, pippin), routines, visits, logs)
    }
}

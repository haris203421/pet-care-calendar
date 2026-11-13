package com.petcare.calendar.data.local

import com.petcare.calendar.domain.model.*
import org.json.JSONArray
import org.json.JSONObject

class LocalDataSerializer {
    private fun obj(vararg pairs: Pair<String, Any?>) = JSONObject().apply { pairs.forEach { put(it.first, it.second ?: JSONObject.NULL) } }
    private fun array(values: Collection<*>) = JSONArray(values)
    private fun JSONObject.rows(key: String): List<JSONObject> = getJSONArray(key).let { a -> (0 until a.length()).map { a.getJSONObject(it) } }
    private fun JSONObject.text(key: String): String? = if (isNull(key)) null else getString(key)
    private fun JSONObject.strings(key: String): List<String> = getJSONArray(key).let { a -> (0 until a.length()).map { a.getString(it) } }

    fun encode(data: CalendarData): String = obj(
        "schema" to 1,
        "pets" to array(data.pets.map { obj("id" to it.id, "name" to it.name, "species" to it.species.name, "photo" to it.photo, "birthday" to it.birthday, "notes" to it.notes, "archived" to it.archived, "created" to it.createdAt) }),
        "routines" to array(data.routines.map { r -> obj("id" to r.id, "pet" to r.petId, "title" to r.title, "category" to r.category.name, "instructions" to r.instructions, "recurrence" to r.recurrence.name, "weekdays" to array(r.weekdays), "interval" to r.interval, "slots" to array(r.slots.map { obj("id" to it.id, "time" to it.time) }), "start" to r.start, "end" to r.end, "enabled" to r.enabled, "archived" to r.archived) }),
        "visits" to array(data.visits.map { obj("id" to it.id, "pet" to it.petId, "reason" to it.reason, "date" to it.date, "time" to it.time, "clinic" to it.clinic, "notes" to it.notes, "reminder" to it.reminder, "status" to it.status.name, "attended" to it.attendedAt, "canceled" to it.canceledAt) }),
        "logs" to array(data.logs.map { obj("id" to it.id, "pet" to it.petId, "name" to it.petName, "species" to it.species.name, "photo" to it.photo, "title" to it.title, "date" to it.date, "time" to it.time, "status" to it.status.name, "instant" to it.recordedAt, "local" to it.recordedLocal, "zone" to it.zone, "note" to it.note, "session" to it.sessionId, "visit" to it.visitId) }),
        "summaries" to array(data.summaries.map { obj("id" to it.id, "session" to it.sessionId, "date" to it.date, "pet" to it.petId, "completed" to it.completed, "skipped" to it.skipped, "remaining" to it.remaining, "new" to array(it.newLogIds), "undo" to it.undoId, "baseline" to array(it.baseline), "created" to it.createdAt) }),
        "settings" to data.settings.let { obj("reminders" to it.reminders, "routineLead" to it.routineLead, "visitLead" to it.visitLead, "timeFormat" to it.timeFormat.name, "haptics" to it.haptics, "motion" to it.reducedMotion, "sample" to it.sampleMode, "since" to it.reminderSince) },
        "delivered" to array(data.delivered)
    ).toString()

    fun decode(value: String): CalendarData {
        val root = JSONObject(value)
        require(root.getInt("schema") == 1)
        val settings = root.getJSONObject("settings")
        return CalendarData(
            pets = root.rows("pets").map { Pet(it.getString("id"), it.getString("name"), Species.valueOf(it.getString("species")), it.getInt("photo"), it.text("birthday"), it.getString("notes"), it.getBoolean("archived"), it.getString("created")) },
            routines = root.rows("routines").map { r -> CareRoutine(r.getString("id"), r.getString("pet"), r.getString("title"), Category.valueOf(r.getString("category")), r.getString("instructions"), Recurrence.valueOf(r.getString("recurrence")), r.getJSONArray("weekdays").let { a -> (0 until a.length()).map { a.getInt(it) }.toSet() }, r.getInt("interval"), r.rows("slots").map { TimeSlot(it.getString("id"), it.getString("time")) }, r.getString("start"), r.text("end"), r.getBoolean("enabled"), r.getBoolean("archived")) },
            visits = root.rows("visits").map { VetAppointment(it.getString("id"), it.getString("pet"), it.getString("reason"), it.getString("date"), it.getString("time"), it.getString("clinic"), it.getString("notes"), it.getBoolean("reminder"), VisitStatus.valueOf(it.getString("status")), it.text("attended"), it.text("canceled")) },
            logs = root.rows("logs").map { CareLog(it.getString("id"), it.getString("pet"), it.getString("name"), Species.valueOf(it.getString("species")), it.getInt("photo"), it.getString("title"), it.getString("date"), it.getString("time"), RecordStatus.valueOf(it.getString("status")), it.getString("instant"), it.getString("local"), it.getString("zone"), it.getString("note"), it.getString("session"), it.text("visit")) },
            summaries = root.rows("summaries").map { CareSessionSummary(it.getString("id"), it.getString("session"), it.getString("date"), it.text("pet"), it.getInt("completed"), it.getInt("skipped"), it.getInt("remaining"), it.strings("new"), it.text("undo"), it.strings("baseline").toSet(), it.getString("created")) },
            settings = AppSettings(settings.getBoolean("reminders"), settings.getInt("routineLead"), settings.getInt("visitLead"), TimeFormat.valueOf(settings.getString("timeFormat")), settings.getBoolean("haptics"), settings.getBoolean("motion"), settings.getBoolean("sample"), settings.optLong("since", 0L)),
            delivered = root.strings("delivered").toSet()
        ).also { data ->
            require(data.pets.map { it.id }.distinct().size == data.pets.size)
            require(data.logs.map { it.id }.distinct().size == data.logs.size)
            data.pets.forEach { require(it.photo in 0..4 && it.name.isNotBlank()); it.birthday?.let(java.time.LocalDate::parse); java.time.Instant.parse(it.createdAt) }
            data.routines.forEach { routine ->
                java.time.LocalDate.parse(routine.start)
                routine.end?.let(java.time.LocalDate::parse)
                require(routine.interval in 1..30 && routine.slots.size in 1..4 && routine.weekdays.all { it in 1..7 })
                require(routine.slots.map { it.id }.distinct().size == routine.slots.size)
                routine.slots.forEach { java.time.LocalTime.parse(it.time) }
            }
            data.visits.forEach { java.time.LocalDate.parse(it.date); java.time.LocalTime.parse(it.time) }
            data.logs.forEach { java.time.LocalDate.parse(it.date); java.time.LocalTime.parse(it.time); java.time.LocalDateTime.parse(it.recordedLocal); java.time.Instant.parse(it.recordedAt) }
            data.summaries.forEach { java.time.LocalDate.parse(it.date); require(it.completed >= 0 && it.skipped >= 0 && it.remaining >= 0) }
        }
    }
}

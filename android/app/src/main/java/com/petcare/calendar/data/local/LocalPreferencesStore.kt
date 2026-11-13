package com.petcare.calendar.data.local

import android.content.Context
import com.petcare.calendar.domain.model.CalendarData

class LocalPreferencesStore(context: Context, private val serializer: LocalDataSerializer) {
    private val preferences = context.getSharedPreferences("pet_care_local_v1", Context.MODE_PRIVATE)

    fun read(): CalendarData? = synchronized(lock) {
        preferences.getString("calendar", null)?.let(serializer::decode)
    }

    fun update(initial: () -> CalendarData, transform: (CalendarData) -> CalendarData): CalendarData = synchronized(lock) {
        val next = transform(read() ?: initial())
        check(preferences.edit().putString("calendar", serializer.encode(next)).commit())
        next
    }

    fun replace(data: CalendarData): CalendarData = synchronized(lock) {
        check(preferences.edit().putString("calendar", serializer.encode(data)).commit())
        data
    }

    companion object {
        private val lock = Any()
    }
}

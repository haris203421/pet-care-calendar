package com.petcare.calendar.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.petcare.calendar.R
import com.petcare.calendar.domain.model.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

val LocalTimeFormat = staticCompositionLocalOf { TimeFormat.SYSTEM }

@Composable
fun uses24Hour(): Boolean = when (LocalTimeFormat.current) {
    TimeFormat.H24 -> true
    TimeFormat.H12 -> false
    TimeFormat.SYSTEM -> android.text.format.DateFormat.is24HourFormat(LocalContext.current)
}

@Composable
fun displayTime(time: String): String = LocalTime.parse(time).format(DateTimeFormatter.ofPattern(if (uses24Hour()) "HH:mm" else "h:mm a"))

fun displayDate(date: String): String = LocalDate.parse(date).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))

@Composable
fun displayTimestamp(timestamp: String): String {
    val local = LocalDateTime.parse(timestamp)
    return stringResource(R.string.date_time, displayDate(local.toLocalDate().toString()), displayTime(local.toLocalTime().toString()))
}

@Composable
fun recurrenceText(routine: CareRoutine): String = when (routine.recurrence) {
    Recurrence.DAILY -> stringResource(R.string.every_day)
    Recurrence.INTERVAL -> stringResource(R.string.every_n_days, routine.interval)
    Recurrence.WEEKDAYS -> routine.weekdays.sorted().joinToString { java.time.DayOfWeek.of(it).getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault()) }
}

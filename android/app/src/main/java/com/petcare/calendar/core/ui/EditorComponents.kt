package com.petcare.calendar.core.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.petcare.calendar.R
import com.petcare.calendar.core.theme.*
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

@Composable
fun ConfirmActionDialog(title: Int, description: Int, confirm: Int, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(title)) }, text = { Text(stringResource(description)) }, confirmButton = { ActionText(confirm, onConfirm) }, dismissButton = { ActionText(R.string.cancel, onDismiss) })
}

@Composable
fun DiscardDialog(show: Boolean, onDiscard: () -> Unit, onKeep: () -> Unit) {
    if (show) AlertDialog(onDismissRequest = onKeep, title = { Text(stringResource(R.string.unsaved_changes)) }, text = { Text(stringResource(R.string.discard_description)) }, confirmButton = { ActionText(R.string.discard_changes, onDiscard) }, dismissButton = { ActionText(R.string.keep_editing, onKeep) })
}

@Composable
fun EditorError(error: Int?) {
    if (error != null) Text(stringResource(error), color = Error, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
}

@Composable
fun EditorField(label: Int, value: String, onValue: (String) -> Unit, lines: Int = 1) {
    OutlinedTextField(value, onValue, modifier = Modifier.fillMaxWidth(), label = { Text(stringResource(label)) }, minLines = lines, singleLine = lines == 1, shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(label: Int, value: String?, onValue: (String?) -> Unit, optional: Boolean = false, latest: String? = null) {
    var open by rememberSaveable { mutableStateOf(false) }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton({ open = true }, Modifier.weight(1f).heightIn(min = 56.dp)) {
            Text(stringResource(R.string.field_value, stringResource(label), value?.let { displayDate(it) } ?: stringResource(R.string.not_set)), style = MaterialTheme.typography.bodyMedium)
        }
        if (optional && value != null) ActionText(R.string.clear, { onValue(null) })
    }
    if (open) {
        val state = rememberDatePickerState(initialSelectedDateMillis = (value?.let(LocalDate::parse) ?: LocalDate.now()).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
        DatePickerDialog(onDismissRequest = { open = false }, confirmButton = {
            TextButton({
                state.selectedDateMillis?.let { millis ->
                    val date = Instant.ofEpochMilli(millis).atOffset(ZoneOffset.UTC).toLocalDate()
                    onValue(date.toString())
                }
                open = false
            }, enabled = state.selectedDateMillis != null && (latest == null || state.selectedDateMillis?.let { Instant.ofEpochMilli(it).atOffset(ZoneOffset.UTC).toLocalDate() <= LocalDate.parse(latest) } == true)) { Text(stringResource(R.string.set_date), style = MaterialTheme.typography.bodyMedium) }
        }, dismissButton = { ActionText(R.string.cancel, { open = false }) }) { DatePicker(state) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeField(value: String, onValue: (String) -> Unit) {
    var open by rememberSaveable { mutableStateOf(false) }
    OutlinedButton({ open = true }, Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text(displayTime(value), style = MaterialTheme.typography.titleMedium) }
    if (open) {
        val time = LocalTime.parse(value)
        val state = rememberTimePickerState(time.hour, time.minute, is24Hour = uses24Hour())
        AlertDialog(onDismissRequest = { open = false }, title = { Text(stringResource(R.string.choose_time)) }, text = { TimePicker(state) }, confirmButton = { ActionText(R.string.set_time, { onValue(LocalTime.of(state.hour, state.minute).toString()); open = false }) }, dismissButton = { ActionText(R.string.cancel, { open = false }) })
    }
}

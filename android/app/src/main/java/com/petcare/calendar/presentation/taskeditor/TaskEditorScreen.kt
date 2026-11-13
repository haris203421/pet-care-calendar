package com.petcare.calendar.presentation.taskeditor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.petcare.calendar.R
import com.petcare.calendar.core.theme.*
import com.petcare.calendar.core.ui.*
import com.petcare.calendar.domain.model.*

@Composable
fun TaskEditorScreen(state: TaskEditorUiState, onBack: () -> Unit, onPet: (String?) -> Unit, onTitle: (String) -> Unit, onCategory: (Category) -> Unit, onInstructions: (String) -> Unit, onRecurrence: (Recurrence) -> Unit, onWeekday: (Int) -> Unit, onInterval: (Int) -> Unit, onTime: (String, String) -> Unit, onAddTime: () -> Unit, onRemoveTime: (String) -> Unit, onStart: (String?) -> Unit, onEnd: (String?) -> Unit, onEnabled: (Boolean) -> Unit, onSave: () -> Unit, onDiscard: () -> Unit, onKeep: () -> Unit, onAddPet: () -> Unit, onRetry: () -> Unit) {
    val routine = state.routine
    BackHandler(onBack = onBack)
    CareLayout(if (state.editing) R.string.edit_task else R.string.add_task_title, onBack = onBack, mode = 2, footer = {
        Text(stringResource(R.string.task_save_hint), color = TextSecondary)
        PrimaryButton(R.string.save_task, onSave, enabled = routine.title.isNotBlank() && state.pets.isNotEmpty() && !state.busy && !state.notFound && state.loadState == LoadState.READY)
    }) {
        if (state.loadState == LoadState.LOADING || state.busy) LoadingCard()
        if (state.loadState == LoadState.ERROR) InlineError(onRetry)
        EditorError(state.error)
        if (state.notFound) EmptyStateCard(R.string.task_missing, R.string.missing_description)
        if (state.pets.isEmpty() && state.loadState == LoadState.READY) { EmptyStateCard(R.string.first_pet, R.string.first_pet_description); SecondaryButton(R.string.add_pet, onAddPet) }
        PetFilterRow(state.pets, routine.petId, onPet, all = false)
        CareSurface {
            EditorField(R.string.task_title, routine.title, onTitle)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Category.entries.forEach { category -> ChoiceChip(stringResource(when (category) { Category.FEEDING -> R.string.feeding; Category.GROOMING -> R.string.grooming; Category.CUSTOM -> R.string.custom }), routine.category == category, { onCategory(category) }) }
            }
            EditorField(R.string.instructions, routine.instructions, onInstructions, 2)
        }
        CareSurface {
            Text(stringResource(R.string.repeat), style = MaterialTheme.typography.titleLarge)
            Column {
                Recurrence.entries.forEach { rule -> ChoiceChip(stringResource(when (rule) { Recurrence.DAILY -> R.string.every_day; Recurrence.WEEKDAYS -> R.string.select_days; Recurrence.INTERVAL -> R.string.interval_days }), routine.recurrence == rule, { onRecurrence(rule) }) }
            }
            if (routine.recurrence == Recurrence.WEEKDAYS) Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..7).forEach { day -> ChoiceChip(java.time.DayOfWeek.of(day).getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault()), day in routine.weekdays, { onWeekday(day) }) }
            }
            if (routine.recurrence == Recurrence.INTERVAL) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SecondaryButton(R.string.less, { onInterval(-1) }, enabled = routine.interval > 1)
                Text(stringResource(R.string.every_n_days, routine.interval), Modifier.weight(1f))
                SecondaryButton(R.string.more, { onInterval(1) }, enabled = routine.interval < 30)
            }
            DateField(R.string.start_date, routine.start, onStart)
            DateField(R.string.end_date, routine.end, onEnd, optional = true)
        }
        CareSurface {
            Text(stringResource(R.string.care_times), style = MaterialTheme.typography.titleLarge)
            routine.slots.forEach { slot ->
                TimeField(slot.time, { onTime(slot.id, it) })
                if (routine.slots.size > 1) ActionText(R.string.remove_time, { onRemoveTime(slot.id) })
            }
            if (routine.slots.size < 4) SecondaryButton(R.string.add_time, onAddTime)
            SettingSwitch(R.string.routine_enabled, routine.enabled, onEnabled)
        }
    }
    DiscardDialog(state.discard, onDiscard, onKeep)
}

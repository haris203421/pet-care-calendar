package com.petcare.calendar.presentation.schedule

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.petcare.calendar.R
import com.petcare.calendar.core.navigation.Screen
import com.petcare.calendar.core.theme.*
import com.petcare.calendar.core.ui.*
import com.petcare.calendar.domain.model.*

@Composable
fun ScheduleScreen(state: ScheduleUiState, onBack: () -> Unit, onNavigate: (Screen) -> Unit, onPet: (String?) -> Unit, onFilter: (Int) -> Unit, onToggle: (String, Boolean) -> Unit, onArchive: (String?) -> Unit, onConfirmArchive: () -> Unit, onRetry: () -> Unit) {
    CareLayout(R.string.schedule, stringResource(R.string.schedule_subtitle), onBack, tab = 2, onNavigate = onNavigate, footer = {
        Text(stringResource(R.string.schedule_hint), color = TextSecondary)
        PrimaryButton(if (state.pets.isEmpty()) R.string.add_pet else R.string.add_task, { onNavigate(if (state.pets.isEmpty()) Screen.PetEditor() else Screen.TaskEditor(petId = state.petId)) })
    }) {
        if (state.loadState == LoadState.LOADING) LoadingCard()
        if (state.loadState == LoadState.ERROR || state.error) InlineError(onRetry)
        PetFilterRow(state.pets, state.petId, onPet)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(R.string.active, R.string.paused, R.string.all).forEachIndexed { i, text -> ChoiceChip(stringResource(text), state.filter == i, { onFilter(i) }) }
        }
        if (state.routines.isEmpty() && state.loadState == LoadState.READY) EmptyStateCard(R.string.no_routines, R.string.no_routines_description)
        state.routines.forEach { routine ->
            CareSurface {
                val pet = state.pets.find { it.id == routine.petId }
                Text(routine.title, style = MaterialTheme.typography.titleLarge)
                Text(pet?.name.orEmpty(), color = AccentDeep, style = MaterialTheme.typography.titleMedium)
                Text(recurrenceText(routine), color = TextSecondary)
                Text(routine.slots.joinToString { it.time }, style = MaterialTheme.typography.bodyLarge)
                if (routine.instructions.isNotBlank()) Text(routine.instructions, color = TextSecondary)
                SettingSwitch(R.string.routine_enabled, routine.enabled, { onToggle(routine.id, it) }, !state.busy)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SecondaryButton(R.string.edit, { onNavigate(Screen.TaskEditor(routine.id, routine.petId)) })
                    ActionText(R.string.archive_task, { onArchive(routine.id) })
                }
            }
        }
    }
    if (state.pendingArchive != null) ConfirmActionDialog(R.string.archive_task, R.string.archive_task_description, R.string.archive_task, onConfirmArchive, { onArchive(null) })
}

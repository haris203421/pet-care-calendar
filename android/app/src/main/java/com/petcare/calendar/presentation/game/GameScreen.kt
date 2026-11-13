package com.petcare.calendar.presentation.game

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.petcare.calendar.R
import com.petcare.calendar.core.navigation.Screen
import com.petcare.calendar.core.theme.*
import com.petcare.calendar.core.ui.*
import com.petcare.calendar.domain.model.*

@Composable
fun GameScreen(state: GameUiState, onBack: () -> Unit, onNavigate: (Screen) -> Unit, onFilter: (String?) -> Unit, onSelect: (String) -> Unit, onRecord: (RecordStatus) -> Unit, onSkip: (Boolean) -> Unit, onNote: (String) -> Unit, onFinish: () -> Unit, onUndo: () -> Unit, onRetry: () -> Unit) {
    val selected = state.items.find { it.id == state.selectedId }
    val progress = stringResource(R.string.care_progress, state.completed, state.items.size)
    CareLayout(R.string.today, subtitle = displayDate(state.date), onBack = onBack, tab = 1, mode = 2, onNavigate = onNavigate, stickyFooter = true, actions = {
        ActionText(R.string.history, { onNavigate(Screen.PetHistory(state.petId)) })
    }, footer = {
        CareSurface {
            if (selected != null) {
                Text(stringResource(R.string.selected_care, selected.petName, selected.title), style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                PrimaryButton(if (selected.visitId == null) R.string.mark_done else R.string.mark_visited, { onRecord(RecordStatus.DONE) }, enabled = !state.busy)
                SecondaryButton(R.string.skip_today, { onSkip(true) }, Modifier.fillMaxWidth(), enabled = !state.busy)
            } else if (state.pets.isNotEmpty()) {
                Text(stringResource(R.string.summary_hint), color = TextSecondary)
                PrimaryButton(R.string.view_summary, onFinish, enabled = !state.busy && state.loadState == LoadState.READY)
            }
            if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            OutlinedButton(onFinish, Modifier.fillMaxWidth().heightIn(min = 48.dp), enabled = !state.busy && state.loadState == LoadState.READY) {
                Image(painterResource(R.drawable.icon_pause), null, Modifier.size(24.dp), contentScale = ContentScale.Fit)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.end_checkin), style = MaterialTheme.typography.bodyMedium)
            }
            if (state.undoId != null) ActionText(R.string.undo_last, onUndo)
        }
    }) {
        if (state.newDay) StatusPill(R.string.new_day)
        if (state.loadState == LoadState.LOADING) LoadingCard()
        if (state.loadState == LoadState.ERROR || state.error) InlineError(onRetry)
        PetFilterRow(state.pets, state.petId, onFilter)
        CareSurface {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.daily_checkin), style = MaterialTheme.typography.titleLarge)
                    Text(progress, Modifier.semantics { liveRegion = LiveRegionMode.Polite; stateDescription = progress }, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
                Image(painterResource(R.drawable.sprite_4), null, Modifier.size(56.dp), contentScale = ContentScale.Fit)
            }
            if (state.items.isNotEmpty()) LinearProgressIndicator(progress = { state.completed.toFloat() / state.items.size }, modifier = Modifier.fillMaxWidth(), color = PrimaryDeep, trackColor = BackgroundLower)
        }
        if (state.saved) Text(stringResource(R.string.care_saved), Modifier.semantics { liveRegion = LiveRegionMode.Polite }, color = PrimaryDeep)
        if (state.items.isEmpty() && state.loadState == LoadState.READY) {
            EmptyStateCard(if (state.pets.isEmpty()) R.string.first_pet else R.string.breathing_room, if (state.pets.isEmpty()) R.string.first_pet_description else R.string.empty_today_description)
            SecondaryButton(if (state.pets.isEmpty()) R.string.add_pet else R.string.add_task, { onNavigate(if (state.pets.isEmpty()) Screen.PetEditor() else Screen.TaskEditor(petId = state.petId)) }, Modifier.fillMaxWidth())
        }
        state.items.forEach { item ->
            CareSurface(selected = item.id == state.selectedId) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PetAvatar(item.photo, item.petName, 48)
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.pet_task, item.petName, item.title), style = MaterialTheme.typography.titleMedium)
                        Text(displayTime(item.time), color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                if (item.instructions.isNotBlank()) Text(item.instructions, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    StatusPill(statusLabel(item.status), item.status == CareStatus.DONE)
                    if (item.log == null) ActionText(if (item.id == state.selectedId) R.string.selected_action else R.string.select, { onSelect(item.id) })
                }
                if (item.log != null) Text(stringResource(R.string.recorded_at, displayTimestamp(item.log.recordedLocal)), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
        }
    }
    if (state.skipDialog) AlertDialog(onDismissRequest = { onSkip(false) }, title = { Text(stringResource(R.string.skip_title)) }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.skip_description, selected?.petName.orEmpty(), selected?.title.orEmpty()))
            OutlinedTextField(state.skipNote, onNote, label = { Text(stringResource(R.string.optional_note)) }, minLines = 2)
        }
    }, confirmButton = { ActionText(R.string.confirm_skip, { onRecord(RecordStatus.SKIPPED) }) }, dismissButton = { ActionText(R.string.keep_care, { onSkip(false) }) })
}

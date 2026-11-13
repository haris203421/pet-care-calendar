package com.petcare.calendar.presentation.history

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
fun PetHistoryScreen(state: PetHistoryUiState, onBack: () -> Unit, onNavigate: (Screen) -> Unit, onPet: (String?) -> Unit, onRange: (Int) -> Unit, onKind: (Int) -> Unit, onClear: () -> Unit, onExpand: (String) -> Unit, onRetry: () -> Unit) {
    CareLayout(R.string.care_history, stringResource(R.string.history_subtitle), onBack, footer = { SecondaryButton(R.string.menu, { onNavigate(Screen.Menu) }, Modifier.fillMaxWidth()) }) {
        PetFilterRow(state.pets, state.petId, onPet)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(7 to R.string.last_seven, 30 to R.string.last_thirty, 0 to R.string.all_time).forEach { ChoiceChip(stringResource(it.second), state.range == it.first, { onRange(it.first) }) }
        }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(R.string.all, R.string.completed_label, R.string.skipped).forEachIndexed { i, label -> ChoiceChip(stringResource(label), state.kind == i, { onKind(i) }) }
        }
        if (state.loadState == LoadState.LOADING) LoadingCard()
        if (state.loadState == LoadState.ERROR) InlineError(onRetry)
        if (state.records.isEmpty() && state.loadState == LoadState.READY) {
            EmptyStateCard(R.string.no_history, R.string.no_history_description)
            SecondaryButton(R.string.change_filters, onClear, Modifier.fillMaxWidth())
            PrimaryButton(R.string.open_today, { onNavigate(Screen.Game()) })
        }
        state.records.groupBy { it.recordedLocal.take(10) }.forEach { (date, records) ->
            Text(displayDate(date), style = MaterialTheme.typography.titleMedium, color = PrimaryDeep)
            records.forEach { log ->
                CareSurface {
                    Text(stringResource(R.string.pet_task, log.petName, log.title), style = MaterialTheme.typography.titleMedium)
                    Text(displayTimestamp(log.recordedLocal), color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    StatusPill(if (log.status == RecordStatus.DONE) R.string.done else R.string.skipped, log.status == RecordStatus.DONE)
                    ActionText(if (state.expandedId == log.id) R.string.hide_details else R.string.details, { onExpand(log.id) })
                    if (state.expandedId == log.id) {
                        Text(stringResource(R.string.scheduled_for, displayDate(log.date), displayTime(log.time)), color = TextSecondary)
                        if (log.note.isNotBlank()) Text(log.note)
                    }
                }
            }
        }
    }
}

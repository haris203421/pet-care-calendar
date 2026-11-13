package com.petcare.calendar.presentation.vetvisits

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
fun VetVisitsScreen(state: VetVisitsUiState, onBack: () -> Unit, onNavigate: (Screen) -> Unit, onPet: (String?) -> Unit, onTab: (Boolean) -> Unit, onExpand: (String) -> Unit, onAsk: (String, VisitAction) -> Unit, onConfirm: () -> Unit, onDismiss: () -> Unit, onRetry: () -> Unit) {
    CareLayout(R.string.vet_visits, stringResource(R.string.vet_subtitle), onBack, tab = 3, onNavigate = onNavigate, mode = 3, footer = {
        Text(stringResource(R.string.vet_hint), color = TextSecondary)
        PrimaryButton(if (state.pets.none { !it.archived }) R.string.add_pet else R.string.add_visit, { onNavigate(if (state.pets.none { !it.archived }) Screen.PetEditor() else Screen.VetVisitEditor(petId = state.petId)) })
    }) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChoiceChip(stringResource(R.string.upcoming), !state.past, { onTab(false) })
            ChoiceChip(stringResource(R.string.past), state.past, { onTab(true) })
        }
        PetFilterRow(state.pets, state.petId, onPet)
        if (state.loadState == LoadState.LOADING) LoadingCard()
        if (state.loadState == LoadState.ERROR || state.error) InlineError(onRetry)
        if (state.visits.isEmpty() && state.loadState == LoadState.READY) EmptyStateCard(R.string.no_visits, if (state.past) R.string.no_past_visits else R.string.no_visits_description)
        state.visits.forEach { visit ->
            val pet = state.pets.find { it.id == visit.petId }
            CareSurface {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    if (pet != null) PetAvatar(pet.photo, pet.name, 52)
                    Column(Modifier.weight(1f)) {
                        Text(visit.reason, style = MaterialTheme.typography.titleLarge)
                        Text(pet?.name.orEmpty(), color = AccentDeep)
                    }
                }
                Text(stringResource(R.string.date_time, displayDate(visit.date), displayTime(visit.time)), color = PrimaryDeep, style = MaterialTheme.typography.titleMedium)
                if (visit.clinic.isNotBlank()) Text(visit.clinic, color = TextSecondary)
                StatusPill(when (visit.status) { VisitStatus.PLANNED -> R.string.planned; VisitStatus.VISITED -> R.string.visited; VisitStatus.CANCELED -> R.string.canceled }, visit.status == VisitStatus.VISITED)
                ActionText(if (state.expandedId == visit.id) R.string.hide_details else R.string.details, { onExpand(visit.id) })
                if (state.expandedId == visit.id) {
                    if (visit.notes.isNotBlank()) Text(visit.notes, color = TextSecondary)
                    SecondaryButton(R.string.edit, { onNavigate(Screen.VetVisitEditor(visit.id, visit.petId)) })
                    if (visit.status == VisitStatus.PLANNED && pet?.archived == false) {
                        SecondaryButton(R.string.mark_visited, { onAsk(visit.id, VisitAction.ATTEND) }, enabled = !state.busy)
                        ActionText(R.string.cancel_visit, { onAsk(visit.id, VisitAction.CANCEL) })
                    } else if (pet?.archived == false) SecondaryButton(R.string.reopen_visit, { onAsk(visit.id, VisitAction.REOPEN) }, enabled = !state.busy)
                }
            }
        }
    }
    if (state.pendingId != null) ConfirmActionDialog(when (state.action) { VisitAction.ATTEND -> R.string.mark_visited; VisitAction.CANCEL -> R.string.cancel_visit; VisitAction.REOPEN -> R.string.reopen_visit }, when (state.action) { VisitAction.ATTEND -> R.string.attend_confirmation; VisitAction.CANCEL -> R.string.cancel_visit_description; VisitAction.REOPEN -> R.string.reopen_description }, R.string.confirm, onConfirm, onDismiss)
}

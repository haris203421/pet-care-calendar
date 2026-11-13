package com.petcare.calendar.presentation.visiteditor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.petcare.calendar.R
import com.petcare.calendar.core.theme.*
import com.petcare.calendar.core.ui.*
import com.petcare.calendar.domain.model.LoadState

@Composable
fun VetVisitEditorScreen(state: VetVisitEditorUiState, onBack: () -> Unit, onPet: (String?) -> Unit, onReason: (String) -> Unit, onDate: (String?) -> Unit, onTime: (String) -> Unit, onClinic: (String) -> Unit, onNotes: (String) -> Unit, onReminder: (Boolean) -> Unit, onSave: () -> Unit, onDiscard: () -> Unit, onKeep: () -> Unit, onAddPet: () -> Unit, onRetry: () -> Unit) {
    BackHandler(onBack = onBack)
    val visit = state.visit
    CareLayout(if (state.editing) R.string.edit_visit else R.string.add_visit_title, onBack = onBack, mode = 3, footer = {
        Text(stringResource(R.string.visit_save_hint), color = TextSecondary)
        PrimaryButton(R.string.save_visit, onSave, enabled = visit.reason.isNotBlank() && state.pets.isNotEmpty() && !state.busy && !state.notFound && state.loadState == LoadState.READY)
    }) {
        if (state.loadState == LoadState.LOADING || state.busy) LoadingCard()
        if (state.loadState == LoadState.ERROR) InlineError(onRetry)
        EditorError(state.error)
        if (state.notFound) EmptyStateCard(R.string.visit_missing, R.string.missing_description)
        if (state.pets.isEmpty() && state.loadState == LoadState.READY) { EmptyStateCard(R.string.first_pet, R.string.first_pet_description); SecondaryButton(R.string.add_pet, onAddPet) }
        PetFilterRow(state.pets, visit.petId, onPet, all = false)
        CareSurface {
            EditorField(R.string.visit_reason, visit.reason, onReason)
            DateField(R.string.visit_date, visit.date, onDate)
            TimeField(visit.time, onTime)
            if (java.time.LocalDate.parse(visit.date) < java.time.LocalDate.now()) Text(stringResource(R.string.past_visit_hint), color = TextSecondary)
            EditorField(R.string.clinic, visit.clinic, onClinic)
            EditorField(R.string.visit_notes, visit.notes, onNotes, 4)
            SettingSwitch(R.string.visit_reminder, visit.reminder, onReminder)
            Text(stringResource(R.string.visit_reminder_hint), color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        }
    }
    DiscardDialog(state.discard, onDiscard, onKeep)
}

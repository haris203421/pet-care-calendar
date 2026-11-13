package com.petcare.calendar.presentation.peteditor

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
fun PetEditorScreen(state: PetEditorUiState, onBack: () -> Unit, onName: (String) -> Unit, onSpecies: (Species) -> Unit, onPhoto: (Int) -> Unit, onBirthday: (String?) -> Unit, onNotes: (String) -> Unit, onSave: () -> Unit, onArchive: (Boolean) -> Unit, onConfirmArchive: () -> Unit, onDiscard: () -> Unit, onKeep: () -> Unit, onRetry: () -> Unit) {
    BackHandler(onBack = onBack)
    CareLayout(if (state.editing) R.string.edit_pet else R.string.add_pet_title, onBack = onBack, mode = 3, footer = {
        Text(stringResource(R.string.pet_save_hint), color = TextSecondary)
        PrimaryButton(R.string.save_pet, onSave, enabled = state.pet.name.isNotBlank() && !state.busy && !state.notFound && state.loadState == LoadState.READY)
        if (state.editing && !state.notFound) ActionText(R.string.archive_pet, { onArchive(true) })
    }) {
        if (state.loadState == LoadState.LOADING || state.busy) LoadingCard()
        if (state.loadState == LoadState.ERROR) InlineError(onRetry)
        EditorError(state.error)
        if (state.notFound) EmptyStateCard(R.string.pet_missing, R.string.missing_description)
        CareSurface {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { PetAvatar(state.pet.photo, state.pet.name, 104) }
            Text(stringResource(R.string.choose_portrait), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.portrait_hint), color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                (0..4).forEach { index ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        PetAvatar(index, stringResource(R.string.portrait_option, index + 1), 56)
                        ChoiceChip(stringResource(R.string.portrait_option, index + 1), state.pet.photo == index, { onPhoto(index) })
                    }
                }
            }
        }
        CareSurface {
            EditorField(R.string.pet_name, state.pet.name, onName)
            Text(stringResource(R.string.species), style = MaterialTheme.typography.titleMedium)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Species.entries.forEach { ChoiceChip(stringResource(speciesLabel(it)), state.pet.species == it, { onSpecies(it) }) }
            }
            DateField(R.string.birthday, state.pet.birthday, onBirthday, optional = true, latest = java.time.LocalDate.now().toString())
            EditorField(R.string.pet_notes, state.pet.notes, onNotes, 3)
        }
    }
    DiscardDialog(state.discard, onDiscard, onKeep)
    if (state.archive) ConfirmActionDialog(R.string.archive_pet, R.string.archive_pet_description, R.string.archive_pet, onConfirmArchive, { onArchive(false) })
}

package com.petcare.calendar.presentation.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.petcare.calendar.R
import com.petcare.calendar.core.navigation.Screen
import com.petcare.calendar.core.theme.*
import com.petcare.calendar.core.ui.*
import com.petcare.calendar.domain.model.LoadState

@Composable
fun MenuScreen(state: MenuUiState, onSelect: (String?) -> Unit, onPlay: () -> Unit, onNavigate: (Screen) -> Unit, onSample: (Boolean) -> Unit, onKeepSamples: () -> Unit, onStartEmpty: () -> Unit, onRetry: () -> Unit, onReminder: () -> Unit) {
    CareLayout(R.string.your_pets, tab = 0, onNavigate = onNavigate, stickyFooter = true, actions = {
        ActionText(R.string.history, { onNavigate(Screen.PetHistory()) })
        ActionText(R.string.settings, { onNavigate(Screen.Settings) })
    }, footer = {
        Text(stringResource(if (state.pets.isEmpty()) R.string.add_pet_to_start else R.string.play_hint), color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        PrimaryButton(R.string.play, onPlay, enabled = state.loadState == LoadState.READY && state.pets.isNotEmpty())
    }) {
        CareSurface {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.hero_title), style = MaterialTheme.typography.titleLarge)
                    Text(if (state.next != null) stringResource(R.string.next_care, state.next.petName, state.next.title, displayTime(state.next.time)) else stringResource(R.string.gentle_routines), color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                }
                Image(painterResource(R.drawable.logo), null, Modifier.size(92.dp), contentScale = ContentScale.Fit)
            }
        }
        if (state.loadState == LoadState.LOADING) LoadingCard()
        if (state.loadState == LoadState.ERROR || state.error) InlineError(onRetry)
        if (state.sampleMode) CareSurface {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.sample_pets), color = PrimaryDeep, style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.sample_description), style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
                ActionText(R.string.use_my_pets, { onSample(true) })
            }
        }
        if (state.pendingDestination != null) SecondaryButton(R.string.open_reminder, onReminder, Modifier.fillMaxWidth())
        if (state.pets.isNotEmpty() && state.due > 0) Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(state.pets.size, R.string.pets_count, Modifier.weight(1f))
            StatCard(state.due, R.string.care_due, Modifier.weight(1f))
        }
        SectionHeading(R.string.pet_profiles, R.string.add_pet, { onNavigate(Screen.PetEditor()) })
        if (state.pets.isEmpty() && state.loadState == LoadState.READY) EmptyStateCard(R.string.first_pet, R.string.first_pet_description)
        if (state.pets.isNotEmpty()) PetFilterRow(state.pets, state.selectedPetId, onSelect)
        BoxWithConstraints {
            val columns = if (maxWidth >= 560.dp) 2 else 1
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                state.pets.chunked(columns).forEach { pets ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        pets.forEach { pet ->
                            Box(Modifier.weight(1f)) { PetProfileCard(pet, pet.id == state.selectedPetId, { onSelect(pet.id) }, { onNavigate(Screen.PetEditor(pet.id)) }) }
                        }
                        if (pets.size < columns) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
    if (state.showSampleChoice) AlertDialog(onDismissRequest = { onSample(false) }, title = { Text(stringResource(R.string.make_it_yours)) }, text = { Text(stringResource(R.string.sample_choice_description)) }, confirmButton = { ActionText(R.string.keep_sample_pets, onKeepSamples) }, dismissButton = { ActionText(R.string.start_empty, onStartEmpty) })
}

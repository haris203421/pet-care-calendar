package com.petcare.calendar.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.petcare.calendar.R
import com.petcare.calendar.core.theme.*
import com.petcare.calendar.domain.model.*

@Composable
fun SecondaryButton(label: Int, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    OutlinedButton(onClick, modifier.heightIn(min = 48.dp), enabled = enabled, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Outline), colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceWhite.copy(alpha = 0.78f), contentColor = PrimaryDeep)) {
        Text(stringResource(label), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ActionText(label: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(onClick, modifier.heightIn(min = 48.dp), colors = ButtonDefaults.textButtonColors(contentColor = AccentDeep)) {
        Text(stringResource(label), fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CareSurface(modifier: Modifier = Modifier, selected: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), color = SurfaceWhite.copy(alpha = 0.94f), border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) AccentDeep else Outline.copy(alpha = 0.45f)), shadowElevation = 3.dp) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable
fun PetAvatar(photo: Int, name: String, size: Int = 64) {
    Image(painterResource(photoResource(photo)), stringResource(R.string.pet_portrait, name), Modifier.size(size.dp).clip(RoundedCornerShape((size / 3).dp)).background(BackgroundLower), contentScale = ContentScale.Fit)
}

fun photoResource(photo: Int): Int = when (photo) {
    0 -> R.drawable.sprite_1
    1 -> R.drawable.sprite_2
    2 -> R.drawable.sprite_3
    3 -> R.drawable.sprite_4
    else -> R.drawable.sprite_5
}

fun speciesLabel(species: Species): Int = when (species) {
    Species.DOG -> R.string.dog
    Species.CAT -> R.string.cat
    Species.RABBIT -> R.string.rabbit
    Species.BIRD -> R.string.bird
}

fun statusLabel(status: CareStatus): Int = when (status) {
    CareStatus.UPCOMING -> R.string.upcoming_status
    CareStatus.DUE -> R.string.due
    CareStatus.OVERDUE -> R.string.overdue
    CareStatus.DONE -> R.string.done
    CareStatus.SKIPPED -> R.string.skipped
}

@Composable
fun StatCard(value: Int, label: Int, modifier: Modifier = Modifier) {
    CareSurface(modifier) {
        Text(stringResource(R.string.number, value), style = MaterialTheme.typography.headlineMedium, color = PrimaryDeep)
        Text(stringResource(label), style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}

@Composable
fun StatusPill(label: Int, positive: Boolean = false) {
    val text = stringResource(label)
    Surface(shape = RoundedCornerShape(50), color = if (positive) BackgroundLower else Secondary.copy(alpha = 0.35f)) {
        Text(text, Modifier.padding(horizontal = 12.dp, vertical = 6.dp).semantics { stateDescription = text }, color = if (positive) PrimaryDeep else TextSecondary, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun PetFilterRow(pets: List<Pet>, selected: String?, onSelect: (String?) -> Unit, all: Boolean = true) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (all) ChoiceChip(stringResource(R.string.all_pets), selected == null, { onSelect(null) })
        pets.forEach { pet -> ChoiceChip(pet.name, selected == pet.id, { onSelect(pet.id) }) }
    }
}

@Composable
fun ChoiceChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val description = stringResource(if (selected) R.string.selected else R.string.not_selected)
    FilterChip(selected, onClick, label = { Text(label, style = MaterialTheme.typography.bodyMedium) }, modifier = Modifier.heightIn(min = 48.dp).semantics { stateDescription = description }, shape = RoundedCornerShape(16.dp), colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Accent.copy(alpha = 0.10f), selectedLabelColor = AccentDeep))
}

@Composable
fun EmptyStateCard(title: Int, description: Int) {
    CareSurface {
        Image(painterResource(R.drawable.sprite_5), null, Modifier.size(64.dp).align(Alignment.CenterHorizontally), contentScale = ContentScale.Fit)
        Text(stringResource(title), style = MaterialTheme.typography.titleLarge)
        Text(stringResource(description), color = TextSecondary, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun InlineError(onRetry: () -> Unit) {
    CareSurface {
        Text(stringResource(R.string.storage_error), color = Error, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
        SecondaryButton(R.string.retry, onRetry)
    }
}

@Composable
fun LoadingCard() {
    CareSurface {
        LinearProgressIndicator(Modifier.fillMaxWidth(), color = PrimaryDeep)
        Text(stringResource(R.string.loading_calendar), color = TextSecondary)
    }
}

@Composable
fun SectionHeading(title: Int, action: Int? = null, onAction: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(title), Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
        if (action != null && onAction != null) ActionText(action, onAction)
    }
}

@Composable
fun PetProfileCard(pet: Pet, selected: Boolean, onSelect: () -> Unit, onEdit: () -> Unit) {
    CareSurface(Modifier.selectable(selected = selected, onClick = onSelect, role = Role.RadioButton), selected) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            PetAvatar(pet.photo, pet.name, 66)
            Column(Modifier.weight(1f)) {
                Text(pet.name, style = MaterialTheme.typography.titleLarge)
                Text(stringResource(speciesLabel(pet.species)), color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
            }
            ActionText(R.string.edit, onEdit)
        }
        if (pet.notes.isNotBlank()) Text(pet.notes, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}

@Composable
fun SettingSwitch(label: Int, checked: Boolean, onChange: (Boolean) -> Unit, enabled: Boolean = true) {
    val description = stringResource(if (checked) R.string.enabled else R.string.disabled)
    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).semantics { stateDescription = description }, verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(label), Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Switch(checked, onChange, enabled = enabled)
    }
}

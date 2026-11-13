package com.petcare.calendar.presentation.settings

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.petcare.calendar.R
import com.petcare.calendar.core.theme.*
import com.petcare.calendar.core.ui.*
import com.petcare.calendar.domain.model.*

@Composable
fun SettingsScreen(state: SettingsUiState, onBack: () -> Unit, onMenu: () -> Unit, onReminders: (Boolean) -> Unit, onSystemSettings: () -> Unit, onRoutineLead: (Int) -> Unit, onVisitLead: (Int) -> Unit, onFormat: (TimeFormat) -> Unit, onHaptics: (Boolean) -> Unit, onMotion: (Boolean) -> Unit, onRestore: (String) -> Unit, onAsk: (DataAction?) -> Unit, onConfirm: () -> Unit, onRetry: () -> Unit) {
    CareLayout(R.string.settings_title, onBack = onBack, mode = 2, footer = { SecondaryButton(R.string.menu, onMenu, Modifier.fillMaxWidth()) }) {
        if (state.loadState == LoadState.LOADING) LoadingCard()
        if (state.error || state.loadState == LoadState.ERROR) InlineError(onRetry)
        if (state.saved) Text(stringResource(R.string.setting_saved), Modifier.semantics { liveRegion = LiveRegionMode.Polite }, color = PrimaryDeep)
        CareSurface {
            Text(stringResource(R.string.reminders), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.reminders_description), color = TextSecondary)
            if (state.settings.sampleMode) Text(stringResource(R.string.sample_reminders_off), color = AccentDeep)
            SettingSwitch(R.string.enable_reminders, state.settings.reminders && state.permission, onReminders, !state.busy && !state.permissionPending && !state.settings.sampleMode && state.loadState == LoadState.READY)
            if (state.permissionPending) { CircularProgressIndicator(Modifier.size(24.dp)); Text(stringResource(R.string.permission_pending)) }
            if (state.permissionDenied || (!state.permission && state.settings.reminders)) {
                Text(stringResource(R.string.permission_denied), color = TextSecondary, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                SecondaryButton(R.string.open_app_settings, onSystemSettings)
            }
            Text(stringResource(R.string.routine_lead), style = MaterialTheme.typography.titleMedium)
            LeadChoices(listOf(0 to R.string.at_time, 10 to R.string.ten_early, 30 to R.string.thirty_early), state.settings.routineLead, onRoutineLead)
            Text(stringResource(R.string.visit_lead), style = MaterialTheme.typography.titleMedium)
            LeadChoices(listOf(30 to R.string.thirty_early, 60 to R.string.hour_early, 1440 to R.string.day_early), state.settings.visitLead, onVisitLead)
            Text(stringResource(R.string.power_saving_hint), style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
        CareSurface {
            Text(stringResource(R.string.appearance), style = MaterialTheme.typography.titleLarge)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(TimeFormat.SYSTEM to R.string.system_format, TimeFormat.H12 to R.string.twelve_hour, TimeFormat.H24 to R.string.twentyfour_hour).forEach { ChoiceChip(stringResource(it.second), state.settings.timeFormat == it.first, { onFormat(it.first) }) }
            }
            SettingSwitch(R.string.haptics, state.settings.haptics, onHaptics, !state.busy)
            SettingSwitch(R.string.reduce_motion, state.settings.reducedMotion, onMotion, !state.busy)
        }
        if (state.archived.isNotEmpty()) CareSurface {
            Text(stringResource(R.string.archived_pets), style = MaterialTheme.typography.titleLarge)
            state.archived.forEach { pet ->
                Row { Text(pet.name, Modifier.weight(1f).padding(top = 12.dp)); ActionText(R.string.restore, { onRestore(pet.id) }) }
            }
            Text(stringResource(R.string.restore_hint), color = TextSecondary)
        }
        CareSurface {
            Text(stringResource(R.string.local_data), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.local_data_description), color = TextSecondary)
            if (state.settings.sampleMode) SecondaryButton(R.string.keep_sample_pets, { onAsk(DataAction.CONVERT) })
            if (state.empty) SecondaryButton(R.string.try_samples, { onAsk(DataAction.SAMPLES) })
            OutlinedButton({ onAsk(DataAction.RESET) }, Modifier.fillMaxWidth().heightIn(min = 48.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Error), enabled = !state.busy) { Text(stringResource(R.string.reset_all_data), style = MaterialTheme.typography.bodyMedium) }
        }
    }
    if (state.pending != null) ConfirmActionDialog(when (state.pending) { DataAction.RESET -> R.string.reset_all_data; DataAction.SAMPLES -> R.string.try_samples; else -> R.string.keep_sample_pets }, if (state.pending == DataAction.RESET) R.string.reset_description else R.string.sample_confirmation, if (state.pending == DataAction.RESET) R.string.delete_local_data else R.string.confirm, onConfirm, { onAsk(null) })
}

@Composable
private fun LeadChoices(options: List<Pair<Int, Int>>, selected: Int, onSelect: (Int) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) { options.forEach { ChoiceChip(stringResource(it.second), selected == it.first, { onSelect(it.first) }) } }
}

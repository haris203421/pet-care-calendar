package com.petcare.calendar.presentation.gameover

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.petcare.calendar.R
import com.petcare.calendar.core.theme.*
import com.petcare.calendar.core.ui.*
import com.petcare.calendar.domain.model.LoadState

@Composable
fun GameOverScreen(state: GameOverUiState, onBack: () -> Unit, onReplay: () -> Unit, onMenu: () -> Unit, onUndo: () -> Unit, onRetry: () -> Unit) {
    val summary = state.summary
    val scale by animateFloatAsState(if (summary == null) 0.92f else 1f, tween(240), label = "summary_badge")
    CareLayout(R.string.care_summary, onBack = onBack, mode = 3, stickyFooter = true, footer = {
        Text(stringResource(R.string.replay_hint), color = TextSecondary)
        PrimaryButton(R.string.play_again, onReplay, enabled = !state.busy)
        SecondaryButton(R.string.menu, onMenu, Modifier.fillMaxWidth())
        if (state.canUndo) SecondaryButton(R.string.undo_last, onUndo, Modifier.fillMaxWidth(), !state.busy)
    }) {
        Image(painterResource(R.drawable.logo), stringResource(R.string.care_summary), Modifier.size(120.dp).scale(scale).align(Alignment.CenterHorizontally), contentScale = ContentScale.Fit)
        if (state.loadState == LoadState.LOADING) LoadingCard()
        if (state.error || state.loadState == LoadState.ERROR) InlineError(onRetry)
        if (summary == null && state.loadState == LoadState.READY) EmptyStateCard(R.string.summary_missing, R.string.summary_missing_description)
        if (summary != null) {
            val outcome = when {
                summary.completed + summary.skipped + summary.remaining == 0 -> R.string.quiet_day
                summary.skipped + summary.remaining == 0 -> R.string.happy_paws
                else -> R.string.checkin_saved
            }
            Text(stringResource(outcome), Modifier.semantics { liveRegion = LiveRegionMode.Polite }, style = MaterialTheme.typography.displaySmall)
            Text(displayDate(summary.date), style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
            CareSurface {
                Text(stringResource(R.string.summary_counts, summary.completed, summary.skipped, summary.remaining), Modifier.semantics { liveRegion = LiveRegionMode.Polite }, style = MaterialTheme.typography.titleMedium)
            }
            val positive = listOf(summary.completed to R.string.completed_label, summary.skipped to R.string.skipped, summary.remaining to R.string.remaining).filter { it.first > 0 }
            if (positive.size >= 2) Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { positive.forEach { StatCard(it.first, it.second, Modifier.weight(1f)) } }
            SectionHeading(R.string.recorded_checkin)
            CareSurface {
                if (state.newLogs.isEmpty()) Text(stringResource(R.string.no_new_care), color = TextSecondary)
                state.newLogs.forEach { log ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        PetAvatar(log.photo, log.petName, 44)
                        Column { Text(log.petName, style = MaterialTheme.typography.titleMedium); Text(log.title, color = TextSecondary) }
                    }
                }
            }
            if (summary.remaining > 0) SecondaryButton(R.string.continue_care, onBack, Modifier.fillMaxWidth())
        }
    }
}

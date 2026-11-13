package com.petcare.calendar.presentation.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.petcare.calendar.R
import com.petcare.calendar.core.theme.*
import com.petcare.calendar.core.ui.PawGradientBackground

@Composable
fun SplashRoute(viewModel: SplashViewModel, onFinished: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val finished by viewModel.finished.collectAsStateWithLifecycle()
    LaunchedEffect(finished) { if (finished && viewModel.consumeFinished()) onFinished() }
    SplashScreen(state)
}

@Composable
fun SplashScreen(state: SplashUiState) {
    val loop = rememberInfiniteTransition(label = "pet_orbit")
    val float by loop.animateFloat(-5f, 5f, infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "float")
    val glow by loop.animateFloat(0.16f, 0.32f, infiniteRepeatable(tween(2400), RepeatMode.Reverse), label = "glow")
    val loading = stringResource(R.string.opening_app)
    PawGradientBackground(1) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(horizontal = 30.dp, vertical = 40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(stringResource(R.string.splash_eyebrow), style = MaterialTheme.typography.labelSmall, color = Secondary)
            Spacer(Modifier.height(36.dp))
            Box(Modifier.size(248.dp, 216.dp), contentAlignment = Alignment.Center) {
                Image(painterResource(R.drawable.logo), null, Modifier.size(210.dp).graphicsLayer { alpha = glow }, contentScale = ContentScale.Fit)
                Image(painterResource(R.drawable.sprite_1), null, Modifier.align(Alignment.TopStart).size(64.dp), contentScale = ContentScale.Fit)
                Image(painterResource(R.drawable.sprite_2), null, Modifier.align(Alignment.TopEnd).size(64.dp), contentScale = ContentScale.Fit)
                Image(painterResource(R.drawable.logo), null, Modifier.size(154.dp).graphicsLayer { translationY = if (state.reducedMotion) 0f else float }, contentScale = ContentScale.Fit)
                Image(painterResource(R.drawable.sprite_3), null, Modifier.align(Alignment.BottomCenter).size(64.dp), contentScale = ContentScale.Fit)
            }
            Spacer(Modifier.height(28.dp))
            Text(stringResource(R.string.app_name), color = SurfaceWhite, style = MaterialTheme.typography.displaySmall, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.splash_subtitle), color = Background, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
            Spacer(Modifier.height(44.dp))
            CircularProgressIndicator(Modifier.size(32.dp).semantics { contentDescription = loading }, color = Secondary, strokeWidth = 3.dp)
            Spacer(Modifier.height(18.dp))
            Text(stringResource(R.string.offline_note), color = Secondary, style = MaterialTheme.typography.labelSmall)
        }
    }
}

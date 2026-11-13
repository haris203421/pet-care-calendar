package com.petcare.calendar.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val CandySweet = lightColorScheme(
    primary = Primary, onPrimary = TextPrimary, primaryContainer = BackgroundLower,
    onPrimaryContainer = SplashBottom, secondary = Secondary, onSecondary = TextPrimary,
    secondaryContainer = Secondary, onSecondaryContainer = SplashBottom,
    tertiary = AccentDeep, onTertiary = SurfaceWhite,
    background = Background, onBackground = TextPrimary,
    surface = SurfaceWhite, onSurface = TextPrimary,
    surfaceVariant = BackgroundLower, onSurfaceVariant = TextSecondary,
    outline = Outline, error = Error, onError = SurfaceWhite
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = CandySweet, typography = AppTypography, content = content)
}

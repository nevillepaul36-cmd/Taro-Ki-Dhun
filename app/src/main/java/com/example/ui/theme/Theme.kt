package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val TunerDarkColorScheme = darkColorScheme(
    primary = TunerGreen,
    onPrimary = DarkBackground,
    primaryContainer = TunerGreenGlow,
    onPrimaryContainer = TunerGreen,
    secondary = TunerCyan,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurfaceElevated,
    onSecondaryContainer = TunerCyan,
    tertiary = GoldAccent,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = TextMuted
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep dark tuner theme consistent like GuitarTuna
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TunerDarkColorScheme,
        typography = Typography,
        content = content
    )
}

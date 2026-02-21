package com.studentbudget.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = White,
    onPrimary = Black,
    secondary = SecondaryGrey,
    onSecondary = White,
    tertiary = MidGrey,
    background = Black,
    onBackground = White,
    surface = DarkGrey,
    onSurface = White,
    surfaceVariant = ElevatedGrey,
    onSurfaceVariant = SecondaryGrey,
    error = Danger,
    onError = White,
    outline = ElevatedGrey,
    outlineVariant = MidGrey,
)

@Composable
fun StudentBudgetTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = AppTypography,
        content = content
    )
}

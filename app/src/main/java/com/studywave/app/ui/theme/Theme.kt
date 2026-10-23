package com.studywave.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val LightScheme = lightColorScheme(
    primary = StudyWaveColors.PrimaryPurple,
    onPrimary = Color.White,
    primaryContainer = StudyWaveColors.LavenderSurface,
    onPrimaryContainer = StudyWaveColors.MainText,
    secondary = StudyWaveColors.SoftBlue,
    onSecondary = Color.White,
    secondaryContainer = StudyWaveColors.LightBlueSurface,
    onSecondaryContainer = StudyWaveColors.MainText,
    background = StudyWaveColors.PrimaryBackground,
    onBackground = StudyWaveColors.MainText,
    surface = StudyWaveColors.PrimaryBackground,
    onSurface = StudyWaveColors.MainText,
    surfaceVariant = StudyWaveColors.SecondaryBackground,
    onSurfaceVariant = StudyWaveColors.SecondaryText,
    outline = StudyWaveColors.Divider,
    error = StudyWaveColors.Error,
    onError = Color.White
)

data class StudyWaveExtendedColors(
    val lavender: Color = StudyWaveColors.LavenderSurface,
    val lightBlue: Color = StudyWaveColors.LightBlueSurface,
    val success: Color = StudyWaveColors.Success,
    val warning: Color = StudyWaveColors.Warning,
    val navInactive: Color = StudyWaveColors.NavInactive
)

val LocalExtendedColors = staticCompositionLocalOf { StudyWaveExtendedColors() }

@Composable
fun StudyWaveTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalExtendedColors provides StudyWaveExtendedColors()) {
        MaterialTheme(
            colorScheme = LightScheme,
            typography = StudyWaveTypography,
            shapes = StudyWaveShapes,
            content = content
        )
    }
}

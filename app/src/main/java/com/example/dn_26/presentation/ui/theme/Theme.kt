package com.example.dn_26.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * 🎨 DRONEX PRO - PROFESSIONAL THEME v2.0
 */

private val DarkColorScheme = darkColorScheme(
    primary = DroneXColors.PrimaryDark,
    secondary = DroneXColors.PurpleAccent,
    tertiary = DroneXColors.PinkAccent,
    background = DroneXColors.BackgroundDark,
    surface = DroneXColors.SurfaceDark,
    error = DroneXColors.ErrorRed,
    onPrimary = DroneXColors.BackgroundDark,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = DroneXColors.PrimaryLight,
    secondary = DroneXColors.PurpleAccent,
    tertiary = DroneXColors.PinkAccent,
    background = DroneXColors.BackgroundLight,
    surface = DroneXColors.SurfaceLight,
    error = DroneXColors.ErrorRed,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onError = Color.White
)

@Composable
fun DroneXProTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ProfessionalTypography,
        content = content
    )
}

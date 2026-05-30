package com.example.dn_26.presentation.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

/**
 * 🎨 THEME MANAGER v2.0
 * Manages professional design system state.
 */

class ProfessionalThemeState(initialDarkMode: Boolean = true) {
    private val _isDarkMode = mutableStateOf(initialDarkMode)
    var isDarkMode: Boolean
        get() = _isDarkMode.value
        set(value) { _isDarkMode.value = value }

    fun toggleTheme() {
        isDarkMode = !isDarkMode
    }
}

val LocalProfessionalTheme = compositionLocalOf<ProfessionalThemeState> {
    error("No ProfessionalThemeState provided")
}

object DroneXTheme {
    val colors: DroneXColors
        @Composable
        @ReadOnlyComposable
        get() = DroneXColors

    val typography = ProfessionalTypography
    val dimensions = DroneXDimensions
}

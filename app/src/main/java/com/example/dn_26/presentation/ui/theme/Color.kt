package com.example.dn_26.presentation.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 🎨 DRONEX PRO - COLOR PALETTE v2.0
 */
object DroneXColors {
    // Primary Colors
    val PrimaryDark = Color(0xFF35E0C2)      // Teal
    val PrimaryLight = Color(0xFF38BDF8)    // Sky
    val PrimaryAccent = PrimaryDark          // Alias for convenience
    
    // Background Colors
    val BackgroundDark = Color(0xFF0D1110)  // Graphite
    val DarkBackground = BackgroundDark      // Alias
    val BackgroundLight = Color(0xFFF8F9FA) // Off-white
    val SurfaceDark = Color(0xFF171C1B)     // Dark graphite
    val SurfaceLight = Color(0xFFFFFFFF)    // White
    
    // Status Colors
    val Success = Color(0xFF10B981)         // Green
    val SuccessGreen = Success              // Alias
    val Warning = Color(0xFFF59E0B)         // Amber
    val WarningYellow = Warning             // Alias
    val Critical = Color(0xFFEF4444)        // Red (Error)
    val ErrorRed = Critical                 // Alias
    val Info = Color(0xFF3B82F6)            // Blue
    val InfoBlue = Info                     // Alias
    
    // Accent Colors
    val SecondaryAccent = Color(0xFF7C3AED) // Purple
    val PurpleAccent = SecondaryAccent      // Alias
    val PinkAccent = Color(0xFFEC4899)      // Pink
    val GreenAccent = Color(0xFF34D399)     // Light Green
    val OrangeAccent = Color(0xFFF97316)    // Orange
}

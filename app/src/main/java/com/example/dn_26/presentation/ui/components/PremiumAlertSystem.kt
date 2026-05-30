package com.example.dn_26.presentation.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dn_26.presentation.ui.theme.DroneXColors

/**
 * 🚨 PREMIUM ALERT SYSTEM v2.0
 * 
 * MONITORING TYPES:
 * 1. Battery Critical
 * 2. Motor Overheat
 * 3. Signal Lost
 * 4. GPS Precision Low
 * 5. Wind Warning
 * 6. Geofence Breach
 * 7. Sensor Failure
 * 8. Obstacle Detected
 * 9. Low Altitude Warning
 */

@Composable
fun PremiumAlertOverlay(
    activeAlerts: List<DroneAlert>
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            activeAlerts.forEach { alert ->
                DroneAlertBanner(alert)
            }
        }
    }
}

data class DroneAlert(
    val type: AlertType,
    val message: String,
    val severity: AlertSeverity
)

enum class AlertType {
    BATTERY, THERMAL, SIGNAL, GPS, WIND, GEOFENCE, SENSOR, OBSTACLE, ALTITUDE
}

enum class AlertSeverity {
    INFO, WARNING, CRITICAL
}

@Composable
fun DroneAlertBanner(alert: DroneAlert) {
    val backgroundColor = when (alert.severity) {
        AlertSeverity.CRITICAL -> DroneXColors.ErrorRed
        AlertSeverity.WARNING -> DroneXColors.WarningYellow
        AlertSeverity.INFO -> DroneXColors.InfoBlue
    }

    val icon = when (alert.type) {
        AlertType.BATTERY -> Icons.Default.BatteryAlert
        AlertType.THERMAL -> Icons.Default.DeviceThermostat
        AlertType.SIGNAL -> Icons.Default.SignalCellularConnectedNoInternet0Bar
        AlertType.GPS -> Icons.Default.LocationDisabled
        AlertType.WIND -> Icons.Default.Air
        AlertType.GEOFENCE -> Icons.Default.Dangerous
        AlertType.SENSOR -> Icons.Default.SettingsSuggest
        AlertType.OBSTACLE -> Icons.Default.Report
        AlertType.ALTITUDE -> Icons.Default.VerticalAlignBottom
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .animateContentSize(),
        color = backgroundColor.copy(alpha = 0.9f),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = alert.type.name,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = alert.message,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

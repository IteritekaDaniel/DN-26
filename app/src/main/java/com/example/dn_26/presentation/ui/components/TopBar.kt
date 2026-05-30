package com.example.dn_26.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dn_26.domain.model.Telemetry
import com.example.dn_26.presentation.viewmodel.DroneControlState
import com.example.dn_26.presentation.ui.theme.DroneXColors

@Composable
fun TopParameterBar(
    telemetry: Telemetry?,
    droneState: DroneControlState,
    onQuickSettingsClick: () -> Unit = {}
) {
    Surface(
        color = Color.Black.copy(alpha = 0.85f),
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Section 1: Connectivity & GPS
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusIndicator(
                    icon = Icons.Default.CellTower,
                    text = "${droneState.connectionProfile.mode.name} ${droneState.droneState.name}",
                    color = if (droneState.droneState.name != "DISCONNECTED") DroneXColors.Success else DroneXColors.Critical
                )
                Spacer(modifier = Modifier.width(24.dp))
                StatusIndicator(
                    icon = Icons.Default.GpsFixed,
                    text = "${telemetry?.gpsSatellites ?: 0} SATS",
                    color = if ((telemetry?.gpsSatellites ?: 0) > 8) DroneXColors.PrimaryAccent else DroneXColors.Warning
                )
            }

            // Section 2: Branding / Mode
            Text(
                text = "${droneState.flightMode} | ${droneState.statusMessage}",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.9f),
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            )

            // Section 3: Power & Config
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusIndicator(
                    icon = Icons.Default.BatteryChargingFull,
                    text = "${droneState.batteryLevel}%",
                    color = when {
                        droneState.batteryLevel > 50 -> DroneXColors.Success
                        droneState.batteryLevel > 20 -> DroneXColors.Warning
                        else -> DroneXColors.Critical
                    }
                )
                Spacer(modifier = Modifier.width(16.dp))
                IconButton(onClick = onQuickSettingsClick) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "System Tuning",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun StatusIndicator(icon: ImageVector, text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            fontWeight = FontWeight.Black
        )
    }
}

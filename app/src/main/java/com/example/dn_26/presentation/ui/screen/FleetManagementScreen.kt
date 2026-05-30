package com.example.dn_26.presentation.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dn_26.presentation.ui.theme.DroneXColors
import com.example.dn_26.presentation.ui.components.GlassCard

@Composable
fun FleetManagementScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DroneXColors.DarkBackground)
            .padding(24.dp)
    ) {
        Text(
            text = "FLEET INVENTORY & ASSETS",
            style = MaterialTheme.typography.headlineMedium,
            color = DroneXColors.PrimaryAccent,
            fontWeight = FontWeight.Black
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            // Liste des Drones
            Column(modifier = Modifier.weight(0.6f)) {
                Text("ACTIVE ASSETS", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                Spacer(Modifier.height(12.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item { DroneAssetItem("DX-PRO-001", "OPERATIONAL", true) }
                    item { DroneAssetItem("DX-PRO-042", "MAINTENANCE", false) }
                    item { DroneAssetItem("DX-LITE-09", "STANDBY", true) }
                }
            }

            // Statistiques Flotte
            Column(modifier = Modifier.weight(0.4f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("HARDWARE STATUS", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                HardwareCard("MOTORS", "88%", DroneXColors.Success)
                HardwareCard("SENSORS", "94%", DroneXColors.Success)
                HardwareCard("COMM-LINK", "100%", DroneXColors.PrimaryAccent)
                HardwareCard("CHASSIS", "72%", DroneXColors.Warning)
            }
        }
    }
}

@Composable
fun DroneAssetItem(id: String, status: String, active: Boolean) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (active) Icons.Default.RocketLaunch else Icons.Default.Build,
                contentDescription = null,
                tint = if (active) DroneXColors.PrimaryAccent else DroneXColors.Warning,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(id, color = Color.White, fontWeight = FontWeight.Bold)
                Text(status, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            }
            if (active) {
                Surface(color = DroneXColors.Success.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                    Text("LIVE", color = DroneXColors.Success, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun HardwareCard(label: String, value: String, color: Color) {
    Surface(
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.weight(1f))
            Text(value, color = color, fontWeight = FontWeight.Black, fontSize = 20.sp)
        }
    }
}

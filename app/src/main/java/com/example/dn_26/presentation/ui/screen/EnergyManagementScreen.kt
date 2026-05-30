package com.example.dn_26.presentation.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Thermostat
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
fun EnergyManagementScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DroneXColors.DarkBackground)
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.BatteryChargingFull,
                contentDescription = null,
                tint = DroneXColors.PrimaryAccent,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "ENERGY & PROPULSION ANALYTICS",
                style = MaterialTheme.typography.headlineMedium,
                color = DroneXColors.PrimaryAccent,
                fontWeight = FontWeight.Black
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            // Power Distribution
            Column(modifier = Modifier.weight(0.5f)) {
                Text("POWER DISTRIBUTION", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                Spacer(Modifier.height(12.dp))
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        PowerMetricRow("MAIN BUS VOLTAGE", "14.8V", 1.0f)
                        PowerMetricRow("CURRENT DRAW", "24.5A", 0.6f)
                        PowerMetricRow("ESC TEMPERATURE", "42°C", 0.4f)
                        PowerMetricRow("CELL BALANCING", "NOMINAL", 1.0f)
                    }
                }
            }

            // Consumption Forecast
            Column(modifier = Modifier.weight(0.5f)) {
                Text("MISSION ENDURANCE", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                Spacer(Modifier.height(12.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        ConsumptionCard("HOVER TIME", "22 min", Icons.Default.Bolt)
                    }
                    item {
                        ConsumptionCard("MAX RANGE", "8.4 km", Icons.Default.Thermostat)
                    }
                }
            }
        }
    }
}

@Composable
fun PowerMetricRow(label: String, value: String, progress: Float) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(value, color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(4.dp),
            color = DroneXColors.PrimaryAccent,
            trackColor = Color.White.copy(alpha = 0.1f)
        )
    }
}

@Composable
fun ConsumptionCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = DroneXColors.Warning, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Text(label, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.weight(1f))
            Text(value, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
        }
    }
}

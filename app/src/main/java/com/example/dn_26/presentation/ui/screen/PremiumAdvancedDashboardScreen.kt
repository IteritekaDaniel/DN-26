package com.example.dn_26.presentation.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dn_26.presentation.ui.theme.DroneXColors
import com.example.dn_26.presentation.ui.components.*

/**
 * 🎨 PREMIUM ADVANCED DASHBOARD v2.0
 */

@Composable
fun PremiumAdvancedDashboardScreen(
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = true,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToFlightMap: () -> Unit = {},
    onNavigateToStats: () -> Unit = {},
    onNavigateToControl: () -> Unit = {}
) {
    var isConnected by remember { mutableStateOf(true) }
    var batteryLevel by remember { mutableIntStateOf(15) } // Low battery to test alerts
    var signalStrength by remember { mutableIntStateOf(95) }
    var altitude by remember { mutableFloatStateOf(45.2f) }
    var speed by remember { mutableFloatStateOf(8.5f) }
    var temperature by remember { mutableFloatStateOf(72.3f) } // High temp to test alerts
    var gpsSignal by remember { mutableIntStateOf(12) }
    
    // Pulse animation for connection
    val pulsing = remember { Animatable(0.8f) }
    LaunchedEffect(isConnected) {
        if (isConnected) {
            while (true) {
                pulsing.animateTo(1.2f, animationSpec = tween(800))
                pulsing.animateTo(0.8f, animationSpec = tween(800))
            }
        }
    }

    // Mock Active Alerts
    val activeAlerts = remember(batteryLevel, temperature) {
        mutableListOf<DroneAlert>().apply {
            if (batteryLevel < 20) add(DroneAlert(AlertType.BATTERY, "Low Battery: $batteryLevel%", AlertSeverity.CRITICAL))
            if (temperature > 70) add(DroneAlert(AlertType.THERMAL, "Motor Overheat: ${temperature}°C", AlertSeverity.WARNING))
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isDarkMode)
                        listOf(DroneXColors.BackgroundDark, DroneXColors.SurfaceDark)
                    else
                        listOf(DroneXColors.BackgroundLight, Color(0xFFE5E7EB))
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                PremiumHeaderV2(
                    isConnected = isConnected,
                    isDarkMode = isDarkMode,
                    onSettingsClick = onNavigateToSettings,
                    pulsingScale = pulsing.value
                )
            }
            
            item {
                AdvancedConnectionStatusCard(
                    isConnected = isConnected,
                    battery = batteryLevel,
                    signalStrength = signalStrength,
                    isDarkMode = isDarkMode,
                    latency = 45
                )
            }
            
            item {
                QuickStatsRowV2(
                    altitude = altitude,
                    speed = speed,
                    temperature = temperature,
                    gpsSignal = gpsSignal,
                    isDarkMode = isDarkMode
                )
            }
            
            item {
                EnhancedMainMetricsSection(
                    batteryLevel = batteryLevel,
                    flightTime = "23m 45s",
                    altitude = altitude,
                    distance = 2.4f,
                    windSpeed = 5.2f,
                    isDarkMode = isDarkMode
                )
            }
            
            item {
                RealTimeMonitoringV2(
                    speed = speed,
                    temperature = temperature,
                    gpsSignal = gpsSignal,
                    isDarkMode = isDarkMode
                )
            }
            
            item {
                EnhancedActionButtonsSection(
                    onMapClick = onNavigateToFlightMap,
                    onStatsClick = onNavigateToStats,
                    onControlClick = onNavigateToControl,
                    isDarkMode = isDarkMode
                )
            }
            
            item { RecentFlightsSectionV2(isDarkMode = isDarkMode) }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        // 🚨 Integrated Alert System Overlay
        PremiumAlertOverlay(activeAlerts = activeAlerts)
    }
}

@Composable
fun PremiumHeaderV2(isConnected: Boolean, isDarkMode: Boolean, onSettingsClick: () -> Unit, pulsingScale: Float) {
    val backgroundColor = if (isDarkMode) DroneXColors.SurfaceDark else DroneXColors.SurfaceLight
    Row(
        modifier = Modifier.fillMaxWidth().background(color = backgroundColor, shape = RoundedCornerShape(16.dp)).padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = "🚁 DroneX Pro", color = DroneXColors.PrimaryDark, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(12.dp * pulsingScale.coerceIn(0.8f, 1.2f)).background(color = if (isConnected) DroneXColors.SuccessGreen else DroneXColors.ErrorRed, shape = CircleShape))
                Text(text = if (isConnected) "🟢 Connected" else "🔴 Offline", color = if (isConnected) DroneXColors.SuccessGreen else DroneXColors.ErrorRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        IconButton(onClick = onSettingsClick, modifier = Modifier.background(DroneXColors.PrimaryDark.copy(alpha = 0.1f), CircleShape)) {
            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = DroneXColors.PrimaryDark)
        }
    }
}

@Composable
fun AdvancedConnectionStatusCard(isConnected: Boolean, battery: Int, signalStrength: Int, isDarkMode: Boolean, latency: Long) {
    val backgroundColor = if (isDarkMode) DroneXColors.SurfaceDark else DroneXColors.SurfaceLight
    Card(modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(16.dp)), colors = CardDefaults.cardColors(containerColor = backgroundColor), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("System Status", color = DroneXColors.PrimaryDark, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                AdvancedStatusItem(icon = Icons.Default.Cloud, label = "Link", value = if (isConnected) "Online" else "Offline", color = if (isConnected) DroneXColors.SuccessGreen else DroneXColors.ErrorRed, modifier = Modifier.weight(1f))
                AdvancedStatusItem(icon = Icons.Default.Speed, label = "Ping", value = "$latency ms", color = DroneXColors.PrimaryDark, modifier = Modifier.weight(1f))
                AdvancedStatusItem(icon = Icons.Default.BatteryChargingFull, label = "Power", value = "$battery%", color = if (battery > 20) DroneXColors.SuccessGreen else DroneXColors.ErrorRed, modifier = Modifier.weight(1f))
                AdvancedStatusItem(icon = Icons.Default.SignalCellularAlt, label = "Signal", value = "$signalStrength%", color = DroneXColors.PurpleAccent, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun AdvancedStatusItem(icon: ImageVector, label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(20.dp))
        Text(text = label, color = DroneXColors.PurpleAccent, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
        Text(text = value, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
fun QuickStatsRowV2(altitude: Float, speed: Float, temperature: Float, gpsSignal: Int, isDarkMode: Boolean) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        EnhancedQuickStatCard("Altitude", "$altitude m", "📍", DroneXColors.PrimaryDark, isDarkMode, Modifier.weight(1f))
        EnhancedQuickStatCard("Speed", "$speed m/s", "⚡", DroneXColors.SuccessGreen, isDarkMode, Modifier.weight(1f))
        EnhancedQuickStatCard("Temp", "$temperature°C", "🌡️", DroneXColors.WarningYellow, isDarkMode, Modifier.weight(1f))
        EnhancedQuickStatCard("GPS", "$gpsSignal sat", "🛰️", DroneXColors.PinkAccent, isDarkMode, Modifier.weight(1f))
    }
}

@Composable
fun EnhancedQuickStatCard(label: String, value: String, emoji: String, color: Color, isDarkMode: Boolean, modifier: Modifier = Modifier) {
    val backgroundColor = if (isDarkMode) DroneXColors.SurfaceDark else DroneXColors.SurfaceLight
    Card(modifier = modifier.shadow(4.dp, RoundedCornerShape(12.dp)), colors = CardDefaults.cardColors(containerColor = backgroundColor), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = emoji, fontSize = 20.sp, modifier = Modifier.padding(bottom = 4.dp))
            Text(text = value, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(text = label, color = DroneXColors.PurpleAccent, fontSize = 9.sp, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

@Composable
fun EnhancedMainMetricsSection(batteryLevel: Int, flightTime: String, altitude: Float, distance: Float, windSpeed: Float, isDarkMode: Boolean) {
    val backgroundColor = if (isDarkMode) DroneXColors.SurfaceDark else DroneXColors.SurfaceLight
    Card(modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(16.dp)), colors = CardDefaults.cardColors(containerColor = backgroundColor), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Flight Metrics", color = DroneXColors.PrimaryDark, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Battery Status", color = DroneXColors.PurpleAccent, fontSize = 12.sp)
                    Text("$batteryLevel%", color = DroneXColors.PrimaryDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                LinearProgressIndicator(progress = { batteryLevel / 100f }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)), color = if (batteryLevel > 20) DroneXColors.SuccessGreen else DroneXColors.ErrorRed, trackColor = if (isDarkMode) DroneXColors.BackgroundDark else Color(0xFFE5E7EB))
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                MetricCard("Time", flightTime, "⏱️", isDarkMode, Modifier.weight(1f))
                MetricCard("Dist", "$distance km", "🛣️", isDarkMode, Modifier.weight(1f))
                MetricCard("Alt", "$altitude m", "📈", isDarkMode, Modifier.weight(1f))
                MetricCard("Wind", "$windSpeed m/s", "💨", isDarkMode, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun MetricCard(label: String, value: String, emoji: String, isDarkMode: Boolean, modifier: Modifier = Modifier) {
    val backgroundColor = if (isDarkMode) DroneXColors.BackgroundDark else Color(0xFFF3F4F6)
    Column(modifier = modifier.background(color = backgroundColor, shape = RoundedCornerShape(8.dp)).padding(12.dp)) {
        Text(text = emoji, fontSize = 16.sp)
        Text(text = label, color = DroneXColors.PurpleAccent, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
        Text(text = value, color = DroneXColors.PrimaryDark, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
fun RealTimeMonitoringV2(speed: Float, temperature: Float, gpsSignal: Int, isDarkMode: Boolean) {
    val backgroundColor = if (isDarkMode) DroneXColors.SurfaceDark else DroneXColors.SurfaceLight
    Card(modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(16.dp)), colors = CardDefaults.cardColors(containerColor = backgroundColor), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Real-Time Telemetry", color = DroneXColors.PrimaryDark, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
            EnhancedGaugeIndicator(label = "Velocity", value = speed.toInt(), maxValue = 50, unit = "m/s", color = DroneXColors.SuccessGreen, isDarkMode = isDarkMode)
            Spacer(modifier = Modifier.height(12.dp))
            EnhancedGaugeIndicator(label = "Core Temp", value = temperature.toInt(), maxValue = 80, unit = "°C", color = DroneXColors.WarningYellow, isDarkMode = isDarkMode)
            Spacer(modifier = Modifier.height(12.dp))
            EnhancedGaugeIndicator(label = "GPS Satellites", value = gpsSignal, maxValue = 20, unit = "sats", color = DroneXColors.PinkAccent, isDarkMode = isDarkMode)
        }
    }
}

@Composable
fun EnhancedGaugeIndicator(label: String, value: Int, maxValue: Int, unit: String, color: Color, isDarkMode: Boolean) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = label, color = DroneXColors.PurpleAccent, fontSize = 11.sp)
            Text(text = "$value $unit", color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(progress = { value.toFloat() / maxValue }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)), color = color, trackColor = if (isDarkMode) DroneXColors.BackgroundDark else Color(0xFFE5E7EB))
    }
}

@Composable
fun EnhancedActionButtonsSection(onMapClick: () -> Unit, onStatsClick: () -> Unit, onControlClick: () -> Unit, isDarkMode: Boolean) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionButtonV2(icon = Icons.Default.Map, label = "Map", color = DroneXColors.PrimaryDark, modifier = Modifier.weight(1f), isDarkMode = isDarkMode, onClick = onMapClick)
            ActionButtonV2(icon = Icons.Default.BarChart, label = "Stats", color = DroneXColors.SuccessGreen, modifier = Modifier.weight(1f), isDarkMode = isDarkMode, onClick = onStatsClick)
        }
        ActionButtonV2(icon = Icons.Default.VideogameAsset, label = "Flight Control", color = DroneXColors.PinkAccent, modifier = Modifier.fillMaxWidth(), isDarkMode = isDarkMode, onClick = onControlClick)
    }
}

@Composable
fun ActionButtonV2(icon: ImageVector, label: String, color: Color, modifier: Modifier = Modifier, isDarkMode: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isDarkMode) DroneXColors.SurfaceDark else DroneXColors.SurfaceLight
    Button(onClick = onClick, modifier = modifier.height(50.dp).shadow(8.dp, RoundedCornerShape(12.dp)), colors = ButtonDefaults.buttonColors(containerColor = backgroundColor), shape = RoundedCornerShape(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(20.dp))
            Text(text = label, color = color, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

@Composable
fun RecentFlightsSectionV2(isDarkMode: Boolean) {
    val backgroundColor = if (isDarkMode) DroneXColors.SurfaceDark else DroneXColors.SurfaceLight
    Card(modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(16.dp)), colors = CardDefaults.cardColors(containerColor = backgroundColor), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Recent History", color = DroneXColors.PrimaryDark, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
            repeat(3) { index -> RecentFlightItemV2(flightNumber = index + 1024, date = "Oct ${12-index}, 2024", duration = "${24 - (index * 3)}m", distance = "${4.2 - (index * 0.8)} km", isDarkMode = isDarkMode) }
        }
    }
}

@Composable
fun RecentFlightItemV2(flightNumber: Int, date: String, duration: String, distance: String, isDarkMode: Boolean) {
    val itemBgColor = if (isDarkMode) DroneXColors.BackgroundDark else Color(0xFFF3F4F6)
    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).background(color = itemBgColor, shape = RoundedCornerShape(8.dp)).clickable { }.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(text = "Flight #$flightNumber", color = DroneXColors.PrimaryDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(text = "$date • $duration • $distance", color = DroneXColors.PurpleAccent, fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp))
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "View", tint = DroneXColors.PurpleAccent, modifier = Modifier.size(20.dp))
    }
}

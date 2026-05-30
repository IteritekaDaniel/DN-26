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

@Composable
fun PremiumDashboardScreen(
    modifier: Modifier = Modifier,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToFlightMap: () -> Unit = {},
    onNavigateToStats: () -> Unit = {}
) {
    var isConnected by remember { mutableStateOf(true) }
    var batteryLevel by remember { mutableIntStateOf(85) }
    var signalStrength by remember { mutableIntStateOf(95) }
    var altitude by remember { mutableFloatStateOf(45.2f) }
    var speed by remember { mutableFloatStateOf(8.5f) }
    var temperature by remember { mutableFloatStateOf(28.3f) }
    var gpsSignal by remember { mutableIntStateOf(12) }
    var flightTime by remember { mutableStateOf("23m 45s") }
    
    val pulsing = remember { Animatable(0.8f) }
    LaunchedEffect(isConnected) {
        if (isConnected) {
            while (true) {
                pulsing.animateTo(1.2f, animationSpec = tween(800))
                pulsing.animateTo(0.8f, animationSpec = tween(800))
            }
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0E27),
                        Color(0xFF1A1E37)
                    )
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
                PremiumHeader(
                    isConnected = isConnected,
                    onSettingsClick = onNavigateToSettings,
                    pulsingScale = pulsing.value
                )
            }
            
            item {
                ConnectionStatusCard(
                    isConnected = isConnected,
                    ipAddress = "192.168.4.1",
                    battery = batteryLevel,
                    signalStrength = signalStrength
                )
            }
            
            item {
                QuickStatsRow(
                    altitude = altitude,
                    speed = speed,
                    temperature = temperature,
                    gpsSignal = gpsSignal
                )
            }
            
            item {
                MainMetricsSection(
                    batteryLevel = batteryLevel,
                    flightTime = flightTime,
                    altitude = altitude
                )
            }
            
            item {
                RealTimeMonitoring(
                    speed = speed,
                    temperature = temperature,
                    gpsSignal = gpsSignal
                )
            }
            
            item {
                ActionButtonsSection(
                    onMapClick = onNavigateToFlightMap,
                    onStatsClick = onNavigateToStats
                )
            }
            
            item {
                RecentFlightsSection()
            }
            
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
fun PremiumHeader(
    isConnected: Boolean,
    onSettingsClick: () -> Unit,
    pulsingScale: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF1A1E37),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "\ud83d\ude81 DroneX Pro",
                color = Color(0xFF00D4FF),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp * pulsingScale.coerceIn(0.8f, 1.2f))
                        .background(
                            color = if (isConnected) Color(0xFF10B981) else Color(0xFFEF4444),
                            shape = CircleShape
                        )
                )
                
                Text(
                    text = if (isConnected) "Connected" else "Disconnected",
                    color = if (isConnected) Color(0xFF10B981) else Color(0xFFEF4444),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .background(
                    color = Color(0xFF00D4FF).copy(alpha = 0.1f),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color(0xFF00D4FF),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun ConnectionStatusCard(
    isConnected: Boolean,
    ipAddress: String,
    battery: Int,
    signalStrength: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1E37)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "System Status",
                color = Color(0xFF00D4FF),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusItem(
                    icon = Icons.Default.Cloud,
                    label = "Connection",
                    value = if (isConnected) "\ud83d\udfe2 Online" else "\ud83d\udd34 Offline",
                    color = if (isConnected) Color(0xFF10B981) else Color(0xFFEF4444),
                    modifier = Modifier.weight(1f)
                )
                
                StatusItem(
                    icon = Icons.Default.Router,
                    label = "IP Address",
                    value = ipAddress,
                    color = Color(0xFF00D4FF),
                    modifier = Modifier.weight(1f)
                )
                
                StatusItem(
                    icon = Icons.Default.BatteryChargingFull,
                    label = "Battery",
                    value = "$battery%",
                    color = when {
                        battery > 50 -> Color(0xFF10B981)
                        battery > 25 -> Color(0xFFF59E0B)
                        else -> Color(0xFFEF4444)
                    },
                    modifier = Modifier.weight(1f)
                )
                
                StatusItem(
                    icon = Icons.Default.SignalCellularAlt,
                    label = "Signal",
                    value = "$signalStrength%",
                    color = Color(0xFF7C3AED),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun StatusItem(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        
        Text(
            text = label,
            color = Color(0xFF7C3AED),
            fontSize = 10.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
        
        Text(
            text = value,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun QuickStatsRow(
    altitude: Float,
    speed: Float,
    temperature: Float,
    gpsSignal: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickStatCard("Altitude", "$altitude m", "\ud83d\udccd", Color(0xFF00D4FF), modifier = Modifier.weight(1f))
        QuickStatCard("Speed", "$speed m/s", "\u26a1", Color(0xFF10B981), modifier = Modifier.weight(1f))
        QuickStatCard("Temperature", "$temperature\u00b0C", "\ud83c\udf21\ufe0f", Color(0xFFF59E0B), modifier = Modifier.weight(1f))
        QuickStatCard("GPS", "$gpsSignal sat", "\ud83d\udce1", Color(0xFFEC4899), modifier = Modifier.weight(1f))
    }
}

@Composable
fun QuickStatCard(
    label: String,
    value: String,
    emoji: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1E37)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 20.sp, modifier = Modifier.padding(bottom = 4.dp))
            Text(
                text = value,
                color = color,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                color = Color(0xFF7C3AED),
                fontSize = 9.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun MainMetricsSection(
    batteryLevel: Int,
    flightTime: String,
    altitude: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1E37)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Main Metrics",
                color = Color(0xFF00D4FF),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                // Circular Progress for Battery
                CircularProgressIndicator(
                    progress = { batteryLevel / 100f },
                    modifier = Modifier.size(160.dp),
                    color = if (batteryLevel > 20) Color(0xFF10B981) else Color(0xFFEF4444),
                    strokeWidth = 12.dp,
                    trackColor = Color(0xFF0A0E27)
                )
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$batteryLevel%",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "BATTERY",
                        color = Color(0xFF7C3AED),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                MetricDetail(label = "Flight Time", value = flightTime)
                VerticalDivider(modifier = Modifier.width(1.dp).height(40.dp), color = Color(0xFF0A0E27))
                MetricDetail(label = "Max Altitude", value = "${altitude + 15}m")
            }
        }
    }
}

@Composable
fun MetricDetail(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color(0xFF7C3AED), fontSize = 11.sp)
        Text(text = value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun RealTimeMonitoring(
    speed: Float,
    temperature: Float,
    gpsSignal: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1E37)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Real-time Monitoring",
                color = Color(0xFF00D4FF),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            MonitoringRow(label = "Speed Velocity", value = "$speed m/s", progress = speed / 20f, color = Color(0xFF10B981))
            Spacer(modifier = Modifier.height(12.dp))
            MonitoringRow(label = "Core Temp", value = "$temperature\u00b0C", progress = temperature / 100f, color = Color(0xFFF59E0B))
            Spacer(modifier = Modifier.height(12.dp))
            MonitoringRow(label = "GPS Accuracy", value = "$gpsSignal satellites", progress = gpsSignal / 24f, color = Color(0xFFEC4899))
        }
    }
}

@Composable
fun MonitoringRow(label: String, value: String, progress: Float, color: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, color = Color.White, fontSize = 12.sp)
            Text(text = value, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .height(6.dp)
                .clip(CircleShape),
            color = color,
            trackColor = Color(0xFF0A0E27)
        )
    }
}

@Composable
fun ActionButtonsSection(
    onMapClick: () -> Unit,
    onStatsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onMapClick,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D4FF)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Map, contentDescription = null, tint = Color(0xFF0A0E27))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Flight Map", color = Color(0xFF0A0E27), fontWeight = FontWeight.Bold)
        }
        
        Button(
            onClick = onStatsClick,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.BarChart, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Analytics", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun RecentFlightsSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Text(
            text = "Recent Flights",
            color = Color(0xFF00D4FF),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        RecentFlightItem(128, "May 24, 2024", "45m", "12.4 km")
        RecentFlightItem(127, "May 22, 2024", "32m", "8.1 km")
        RecentFlightItem(126, "May 20, 2024", "18m", "4.5 km")
    }
}

@Composable
fun RecentFlightItem(
    flightNumber: Int,
    date: String,
    duration: String,
    distance: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .background(
                color = Color(0xFF1A1E37),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF0A0E27), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.FlightTakeoff, contentDescription = null, tint = Color(0xFF00D4FF), modifier = Modifier.size(20.dp))
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = "Flight #$flightNumber",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$date \u2022 $duration",
                    color = Color(0xFF7C3AED),
                    fontSize = 12.sp
                )
            }
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "View",
            tint = Color(0xFF7C3AED),
            modifier = Modifier.size(20.dp)
        )
    }
}

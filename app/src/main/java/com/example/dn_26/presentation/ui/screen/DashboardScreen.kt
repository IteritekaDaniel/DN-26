package com.example.dn_26.presentation.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dn_26.domain.model.DroneCommand
import com.example.dn_26.domain.model.Alert
import com.example.dn_26.domain.model.AlertSeverity
import com.example.dn_26.presentation.viewmodel.DroneControlViewModel
import com.example.dn_26.presentation.viewmodel.TelemetryViewModel
import com.example.dn_26.presentation.viewmodel.AlertViewModel
import com.example.dn_26.presentation.ui.theme.DroneXColors
import com.example.dn_26.presentation.ui.components.*
import com.example.dn_26.presentation.ui.gamepad.EliteGamepad

/**
 * 🛰️ DRONEX PRO - ULTIMATE MISSION DASHBOARD v3.5
 * 
 * L'interface tactique la plus complète :
 * - PFD (Primary Flight Display) avec échelles de vol.
 * - Horizon Artificiel avec Pitch Ladder.
 * - Monitoring IA & Propulsion.
 * - Elite Gamepad Integration.
 */
@Composable
fun DashboardScreen(
    droneControlViewModel: DroneControlViewModel,
    telemetryViewModel: TelemetryViewModel,
    alertViewModel: AlertViewModel,
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val droneControlState by droneControlViewModel.state.collectAsState()
    val telemetryState by telemetryViewModel.state.collectAsState()
    val alertState by alertViewModel.state.collectAsState()

    val telemetry = telemetryState.latestTelemetry
    val recentAlerts = alertState.alerts.take(4)
    
    var showGamepad by remember { mutableStateOf(true) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(DroneXColors.BackgroundDark, Color(0xFF020208))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // ==================== BARRE DE STATUT SUPÉRIEURE ====================
            DashboardHeader(
                state = droneControlState,
                onSettingsClick = onNavigateToSettings
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ==================== ZONE PRINCIPALE (TRIPLE PANEL) ====================
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // --- PANEL GAUCHE : SYSTÈMES ET DIAGNOSTICS ---
                Column(
                    modifier = Modifier.weight(0.25f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PanelHeader("SYSTEM AVIONICS", Icons.Default.Dns)
                    
                    ConnectivityCard(
                        latency = droneControlState.latency,
                        quality = droneControlState.connectionQuality
                    )

                    MotorStatusCard(health = droneControlState.motorHealth)

                    PanelHeader("AI ANALYSIS", Icons.Default.AutoAwesome)
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            MetricRow("RISK SCORE", "2.4%", DroneXColors.Success)
                            MetricRow("ANOMALIES", "NONE", DroneXColors.Success)
                        }
                    }

                    PanelHeader("MISSION LOGS", Icons.AutoMirrored.Filled.Assignment)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        recentAlerts.forEach { alert ->
                            IncidentItem(alert)
                        }
                    }
                }

                // --- PANEL CENTRAL : PFD (PRIMARY FLIGHT DISPLAY) ---
                Column(
                    modifier = Modifier.weight(0.5f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.Black.copy(alpha = 0.5f))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Radar de fond
                        TacticalRadar(modifier = Modifier.size(350.dp).alpha(0.4f))
                        
                        // HUD Principal
                        PFDOverlay(telemetry)
                        
                        // Boussole supérieure
                        AviationCompass(
                            heading = telemetry?.yaw?.toFloat() ?: 0f,
                            modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp)
                        )
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    
                    // Grille de télémétrie rapide
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item { MiniStatCard("ALT", "${telemetry?.altitude?.toInt() ?: 0}m", Icons.Default.VerticalAlignTop) }
                        item { MiniStatCard("BATT", "${droneControlState.batteryLevel}%", Icons.Default.BatteryChargingFull) }
                        item { MiniStatCard("GPS", "${telemetry?.gpsSatellites ?: 0} SV", Icons.Default.Public) }
                        item { MiniStatCard("WIND", "${"%.1f".format(telemetry?.windSpeed ?: 0.0)}", Icons.Default.Air) }
                    }
                }

                // --- PANEL DROIT : MISSION CONTROL & ACTIONS ---
                Column(
                    modifier = Modifier.weight(0.25f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PanelHeader("FLIGHT CONTROL", Icons.Default.Gamepad)
                    
                    val isFlying = droneControlState.droneState.name.contains("FLYING") || 
                                   droneControlState.droneState.name.contains("TAKING_OFF")

                    MissionButton(
                        text = if (isFlying) "INITIATE LANDING" else "LAUNCH DRONE",
                        icon = if (isFlying) Icons.Default.FlightLand else Icons.Default.FlightTakeoff,
                        onClick = { 
                            if (isFlying) droneControlViewModel.executeCommand(DroneCommand.LAND) 
                            else droneControlViewModel.executeCommand(DroneCommand.TAKEOFF)
                        },
                        color = if (isFlying) DroneXColors.Warning else DroneXColors.PrimaryAccent
                    )

                    MissionButton(
                        text = "RETURN TO HOME",
                        icon = Icons.Default.Home,
                        onClick = { droneControlViewModel.executeCommand(DroneCommand.RETURN_HOME) },
                        color = DroneXColors.Info
                    )

                    MissionButton(
                        text = "CALIBRATE IMU",
                        icon = Icons.Default.Architecture,
                        onClick = { droneControlViewModel.executeCommand(DroneCommand.CALIBRATE) },
                        color = DroneXColors.SecondaryAccent
                    )

                    Spacer(Modifier.weight(1f))

                    MissionButton(
                        text = "EMERGENCY KILL",
                        icon = Icons.Default.Report,
                        onClick = { droneControlViewModel.emergencyStop() },
                        color = DroneXColors.Critical
                    )
                }
            }

            // ==================== COUCHE INTERACTIVE : ELITE GAMEPAD ====================
            AnimatedVisibility(
                visible = showGamepad,
                enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut()
            ) {
                EliteGamepad(
                    onCommand = { droneControlViewModel.executeCommand(it) },
                    onMovement = { x, y, z, rotation ->
                        droneControlViewModel.updateJoystickInput(x, y, z, rotation)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .padding(top = 12.dp)
                )
            }
            
            // Toggle de la manette
            IconButton(
                onClick = { showGamepad = !showGamepad },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 4.dp)
                    .background(DroneXColors.SurfaceDark, CircleShape)
                    .size(28.dp)
            ) {
                Icon(
                    imageVector = if (showGamepad) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                    contentDescription = null,
                    tint = DroneXColors.PrimaryAccent,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun DashboardHeader(state: com.example.dn_26.presentation.viewmodel.DroneControlState, onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "🚁 DroneX PRO MISSION",
                color = DroneXColors.PrimaryAccent,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                val qualityColor = when(state.connectionQuality) {
                    "EXCELLENT" -> DroneXColors.Success
                    "GOOD" -> DroneXColors.PrimaryDark
                    "WARNING" -> DroneXColors.Warning
                    else -> DroneXColors.Critical
                }
                Box(modifier = Modifier.size(8.dp).background(qualityColor, CircleShape))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "LINK: ${state.connectionQuality} :: MODE: ${state.droneState.name} :: ${formatUptime(state.uptime)}",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            ConnectivityBars(state.signalStrength)
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.background(DroneXColors.SurfaceDark, RoundedCornerShape(8.dp)).size(40.dp)
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = DroneXColors.PrimaryAccent, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun PanelHeader(title: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 6.dp)) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ConnectivityCard(latency: Long, quality: String) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Column {
                Text("LATENCY", fontSize = 8.sp, color = Color.Gray)
                Text("${latency}ms", fontSize = 12.sp, color = if(latency < 100) DroneXColors.Success else DroneXColors.Warning, fontWeight = FontWeight.Black)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("QUALITY", fontSize = 8.sp, color = Color.Gray)
                Text(quality, fontSize = 12.sp, color = DroneXColors.Info, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun PFDOverlay(telemetry: com.example.dn_26.domain.model.Telemetry?) {
    Box(modifier = Modifier.fillMaxSize()) {
        // --- Échelle de Vitesse (Gauche) ---
        PFDTape(
            value = telemetry?.speed ?: 0.0,
            label = "SPD",
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 24.dp)
        )

        // --- Échelle d'Altitude (Droite) ---
        PFDTape(
            value = telemetry?.altitude ?: 0.0,
            label = "ALT",
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 24.dp)
        )

        // --- Horizon Artificiel Central ---
        Canvas(modifier = Modifier.size(150.dp).align(Alignment.Center)) {
            val roll = telemetry?.roll?.toFloat() ?: 0f
            val pitch = telemetry?.pitch?.toFloat() ?: 0f
            
            rotate(roll) {
                // Ligne d'horizon
                drawLine(
                    color = DroneXColors.PrimaryAccent,
                    start = Offset(0f, size.height/2 + pitch.dp.toPx()),
                    end = Offset(size.width, size.height/2 + pitch.dp.toPx()),
                    strokeWidth = 2.dp.toPx()
                )
                
                // Pitch Ladder (Échelons de tangage)
                for (i in -30..30 step 10) {
                    if (i == 0) continue
                    val y = size.height/2 + pitch.dp.toPx() - (i * 2).dp.toPx()
                    drawLine(
                        color = Color.White.copy(alpha = 0.3f),
                        start = Offset(size.width/4, y),
                        end = Offset(size.width*3/4, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
            
            // Marqueur central fixe
            drawCircle(color = Color.Red, radius = 4f, center = Offset(size.width/2, size.height/2))
            drawLine(color = Color.Red, start = Offset(size.width/2 - 20f, size.height/2), end = Offset(size.width/2 + 20f, size.height/2), strokeWidth = 2f)
        }
    }
}

@Composable
fun PFDTape(value: Double, label: String, modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(150.dp)
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${value.toInt()}",
                fontSize = 18.sp,
                color = DroneXColors.PrimaryAccent,
                fontWeight = FontWeight.Black
            )
            // Marqueurs gradués
            Canvas(modifier = Modifier.fillMaxSize()) {
                for (i in 0..10) {
                    val y = (size.height / 10) * i
                    drawLine(Color.White.copy(alpha = 0.2f), Offset(0f, y), Offset(8f, y), 1.dp.toPx())
                }
            }
        }
    }
}

@Composable
fun MotorStatusCard(health: String) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text("PROPULSION STATUS", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 8.sp)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                repeat(4) { i ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.size(6.dp).background(
                            if (health == "GOOD") DroneXColors.Success else DroneXColors.Critical, CircleShape
                        ))
                        Text("M${i+1}", fontSize = 7.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun MetricRow(label: String, value: String, color: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(value, fontSize = 11.sp, color = color, fontWeight = FontWeight.Black)
    }
}

@Composable
fun ConnectivityBars(signal: Int) {
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        repeat(5) { i ->
            val active = signal > (i * 20)
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height((6 + (i * 3)).dp)
                    .background(if (active) DroneXColors.PrimaryAccent else Color(0xFF202030), RoundedCornerShape(1.dp))
            )
        }
    }
}

@Composable
fun MissionButton(text: String, icon: ImageVector, onClick: () -> Unit, color: Color) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth().height(50.dp),
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(12.dp))
            Text(text.uppercase(), color = color, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
        }
    }
}

@Composable
fun IncidentItem(alert: Alert) {
    Surface(
        color = Color.White.copy(alpha = 0.04f),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(5.dp).background(
                when(alert.severity) {
                    AlertSeverity.CRITICAL -> DroneXColors.Critical
                    AlertSeverity.MEDIUM -> DroneXColors.Warning
                    else -> DroneXColors.Info
                }, CircleShape
            ))
            Spacer(Modifier.width(10.dp))
            Column {
                Text(alert.title, style = MaterialTheme.typography.labelLarge, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(alert.message, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1, fontSize = 8.sp)
            }
        }
    }
}

@Composable
fun MiniStatCard(label: String, value: String, icon: ImageVector) {
    Surface(
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
    ) {
        Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = Color.Gray.copy(alpha = 0.6f), modifier = Modifier.size(12.dp))
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 7.sp)
            Text(value, style = MaterialTheme.typography.titleSmall, color = DroneXColors.PrimaryAccent, fontWeight = FontWeight.Black, fontSize = 13.sp)
        }
    }
}

fun formatUptime(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return "${m}M ${s}S"
}

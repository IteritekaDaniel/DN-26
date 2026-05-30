package com.example.dn_26.presentation.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.FlightLand
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dn_26.domain.model.AlertSeverity
import com.example.dn_26.domain.model.ConnectionMode
import com.example.dn_26.domain.model.ConnectionProfile
import com.example.dn_26.domain.model.DroneCommand
import com.example.dn_26.domain.model.DroneState
import com.example.dn_26.domain.model.Telemetry
import com.example.dn_26.presentation.ui.theme.DroneXColors
import com.example.dn_26.presentation.viewmodel.AIState
import com.example.dn_26.presentation.viewmodel.AlertState
import com.example.dn_26.presentation.viewmodel.CommandLogItem
import com.example.dn_26.presentation.viewmodel.DroneControlState
import com.example.dn_26.presentation.viewmodel.TelemetryState
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun MissionControlDashboardScreen(
    droneState: DroneControlState,
    telemetryState: TelemetryState,
    alertState: AlertState,
    aiState: AIState,
    onConnect: (ConnectionProfile) -> Unit,
    onDisconnect: () -> Unit,
    onCommand: (DroneCommand) -> Unit,
    onNavigateToControl: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    var selectedMode by remember(droneState.connectionProfile.mode) {
        mutableStateOf(droneState.connectionProfile.mode)
    }
    var ipAddress by remember(droneState.connectionProfile.ipAddress) {
        mutableStateOf(droneState.connectionProfile.ipAddress)
    }
    var portText by remember(droneState.connectionProfile.port) {
        mutableStateOf(droneState.connectionProfile.port.toString())
    }
    var bluetoothAddress by remember(droneState.connectionProfile.bluetoothAddress) {
        mutableStateOf(droneState.connectionProfile.bluetoothAddress)
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        DroneXColors.BackgroundDark,
                        Color(0xFF101715),
                        Color(0xFF17140F)
                    )
                )
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1.35f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardHeader(
                droneState = droneState,
                alertState = alertState,
                onNavigateToSettings = onNavigateToSettings
            )

            TelemetryGrid(
                telemetry = telemetryState.latestTelemetry,
                telemetryState = telemetryState,
                droneState = droneState
            )

            MissionReadinessPanel(
                droneState = droneState,
                telemetry = telemetryState.latestTelemetry,
                recentTelemetry = telemetryState.recentTelemetry,
                alertState = alertState,
                aiState = aiState
            )

            QuickCommandPanel(
                droneState = droneState,
                onCommand = onCommand,
                onNavigateToControl = onNavigateToControl,
                onNavigateToMap = onNavigateToMap,
                onNavigateToAnalytics = onNavigateToAnalytics
            )
        }

        Column(
            modifier = Modifier
                .width(350.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ConnectionPanel(
                selectedMode = selectedMode,
                onModeChange = { selectedMode = it },
                ipAddress = ipAddress,
                onIpChange = { ipAddress = it },
                portText = portText,
                onPortChange = { portText = it.filter(Char::isDigit).take(5) },
                bluetoothAddress = bluetoothAddress,
                onBluetoothChange = { bluetoothAddress = it.uppercase().take(17) },
                droneState = droneState,
                onConnect = {
                    onConnect(
                        ConnectionProfile(
                            mode = selectedMode,
                            ipAddress = ipAddress.ifBlank { "192.168.4.1" },
                            port = portText.toIntOrNull()?.coerceIn(1, 65535) ?: 8080,
                            bluetoothAddress = bluetoothAddress
                        )
                    )
                },
                onDisconnect = onDisconnect
            )

            EngineerPreflightPanel(
                droneState = droneState,
                telemetry = telemetryState.latestTelemetry,
                alertState = alertState,
                aiState = aiState
            )

            BlackboxPanel(
                telemetryState = telemetryState,
                commandHistory = droneState.commandHistory
            )

            LiveAnalysisPanel(
                aiState = aiState,
                alertState = alertState,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DashboardHeader(
    droneState: DroneControlState,
    alertState: AlertState,
    onNavigateToSettings: () -> Unit
) {
    Surface(
        color = DroneXColors.SurfaceDark.copy(alpha = 0.92f),
        shape = RoundedCornerShape(10.dp),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(DroneXColors.PrimaryAccent.copy(alpha = 0.14f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Radar, contentDescription = null, tint = DroneXColors.PrimaryAccent)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("DroneX Ground Station", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
                    Text(
                        "${droneState.connectionProfile.mode.name.replace('_', ' ')} | ${droneState.flightMode} | ${droneState.statusMessage}",
                        color = Color.White.copy(alpha = 0.62f),
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = onNavigateToSettings,
                    label = { Text("${alertState.criticalCount} critical") },
                    leadingIcon = {
                        Icon(
                            if (alertState.criticalCount > 0) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (alertState.criticalCount > 0) DroneXColors.Critical else DroneXColors.Success
                        )
                    }
                )
                IconButton(onClick = onNavigateToSettings) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun TelemetryGrid(
    telemetry: Telemetry?,
    telemetryState: TelemetryState,
    droneState: DroneControlState
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        TelemetryTile(
            label = "Altitude",
            value = "${telemetry?.altitude?.roundToInt() ?: 0} m",
            detail = "Max ${telemetryState.maxAltitude.roundToInt()} m",
            icon = Icons.Default.Route,
            color = DroneXColors.PrimaryAccent,
            modifier = Modifier.weight(1f)
        )
        TelemetryTile(
            label = "Speed",
            value = "${telemetry?.speed?.let { (it * 3.6).roundToInt() } ?: 0} km/h",
            detail = "Avg ${(telemetryState.averageSpeed * 3.6).roundToInt()} km/h",
            icon = Icons.Default.Speed,
            color = DroneXColors.GreenAccent,
            modifier = Modifier.weight(1f)
        )
        TelemetryTile(
            label = "Battery",
            value = "${telemetryState.batteryPercent.coerceAtMost(droneState.batteryLevel)}%",
            detail = "${telemetry?.batteryVoltage?.format1() ?: "12.6"} V",
            icon = Icons.Default.BatteryChargingFull,
            color = batteryColor(telemetryState.batteryPercent),
            modifier = Modifier.weight(1f)
        )
        TelemetryTile(
            label = "GPS",
            value = "${telemetry?.gpsSatellites ?: 0} sats",
            detail = "${telemetry?.gpsSignalStrength ?: 0}% signal",
            icon = Icons.Default.GpsFixed,
            color = if ((telemetry?.gpsSatellites ?: 0) >= 8) DroneXColors.Success else DroneXColors.Warning,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TelemetryTile(
    label: String,
    value: String,
    detail: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    val targetFill = when (label) {
        "Battery" -> value.removeSuffix("%").toFloatOrNull()?.div(100f) ?: 0f
        "GPS" -> ((detail.substringBefore("%").toFloatOrNull() ?: 0f) / 100f).coerceIn(0f, 1f)
        else -> 0.74f
    }
    val animatedFill by animateFloatAsState(
        targetValue = targetFill.coerceIn(0f, 1f),
        animationSpec = tween(450),
        label = "telemetry_tile"
    )

    Card(
        modifier = modifier.height(118.dp),
        colors = CardDefaults.cardColors(containerColor = DroneXColors.SurfaceDark.copy(alpha = 0.94f)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(label.uppercase(), color = Color.White.copy(alpha = 0.58f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Text(value, color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp)
            Text(detail, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            LinearProgressIndicator(
                progress = { animatedFill },
                modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(3.dp)),
                color = color,
                trackColor = Color.White.copy(alpha = 0.06f)
            )
        }
    }
}

@Composable
private fun MissionReadinessPanel(
    droneState: DroneControlState,
    telemetry: Telemetry?,
    recentTelemetry: List<Telemetry>,
    alertState: AlertState,
    aiState: AIState
) {
    Surface(
        color = DroneXColors.SurfaceDark.copy(alpha = 0.9f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FlightAttitudePreview(
                pitch = telemetry?.pitch ?: 0.0,
                roll = telemetry?.roll ?: 0.0,
                modifier = Modifier.size(168.dp)
            )

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ReadinessRow(
                    "Link",
                    droneState.connectionQuality,
                    droneState.droneState != DroneState.DISCONNECTED && droneState.droneState != DroneState.ERROR
                )
                ReadinessRow("Battery", "${((telemetry?.batteryVoltage ?: 12.6) / 12.6 * 100).roundToInt()}%", (telemetry?.batteryVoltage ?: 12.6) > 11.1)
                ReadinessRow("GPS", "${telemetry?.gpsSatellites ?: 0} satellites", (telemetry?.gpsSatellites ?: 0) >= 8)
                ReadinessRow("AI risk", "${aiState.riskScore.roundToInt()}/100", aiState.riskScore < 55.0)
                ReadinessRow("Alerts", "${alertState.unreadCount} active", alertState.criticalCount == 0)
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Live Telemetry Stream", color = Color.White, fontWeight = FontWeight.Bold)
                Sparkline(samples = recentTelemetry, color = DroneXColors.PrimaryAccent)
                Text(
                    "Samples: ${recentTelemetry.size} | AI ${if (aiState.lastAnalysisTimestamp > 0) "online" else "waiting"}",
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun FlightAttitudePreview(
    pitch: Double,
    roll: Double,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2
            val rollOffset = (roll / 45.0).toFloat().coerceIn(-1f, 1f) * radius * 0.25f
            val pitchOffset = (pitch / 45.0).toFloat().coerceIn(-1f, 1f) * radius * 0.45f

            drawCircle(Color.White.copy(alpha = 0.08f), radius = radius, center = center)
            drawCircle(Color.White.copy(alpha = 0.18f), radius = radius - 2.dp.toPx(), center = center, style = Stroke(1.dp.toPx()))
            drawLine(
                DroneXColors.PrimaryAccent,
                Offset(center.x - radius * 0.65f, center.y + pitchOffset + rollOffset),
                Offset(center.x + radius * 0.65f, center.y + pitchOffset - rollOffset),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawCircle(DroneXColors.Warning, 5.dp.toPx(), center)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("ATT", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text("${pitch.roundToInt()} / ${roll.roundToInt()}", color = Color.White, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun Sparkline(samples: List<Telemetry>, color: Color) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(74.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.2f))
            .padding(8.dp)
    ) {
        val points = if (samples.size >= 2) samples.takeLast(40).map { it.altitude.toFloat() } else listOf(8f, 12f, 11f, 18f, 16f, 24f, 20f)
        val min = points.minOrNull() ?: 0f
        val max = points.maxOrNull() ?: 1f
        val range = (max - min).coerceAtLeast(1f)
        val step = size.width / (points.size - 1).coerceAtLeast(1)
        val path = Path()
        points.forEachIndexed { index, value ->
            val x = index * step
            val y = size.height - ((value - min) / range) * size.height
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
    }
}

@Composable
private fun ReadinessRow(label: String, value: String, ok: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (ok) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = if (ok) DroneXColors.Success else DroneXColors.Warning,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(label, color = Color.White.copy(alpha = 0.72f), fontSize = 12.sp)
        }
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
private fun QuickCommandPanel(
    droneState: DroneControlState,
    onCommand: (DroneCommand) -> Unit,
    onNavigateToControl: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToAnalytics: () -> Unit
) {
    Surface(color = DroneXColors.SurfaceDark.copy(alpha = 0.9f), shape = RoundedCornerShape(10.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Command Deck", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                Text(droneState.lastCommand?.name?.replace('_', ' ') ?: "No command", color = DroneXColors.PrimaryAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                CommandButton(Icons.Default.Shield, if (droneState.isArmed) "Disarm" else "Arm", DroneXColors.PrimaryAccent, Modifier.weight(1f)) {
                    onCommand(if (droneState.isArmed) DroneCommand.DISARM else DroneCommand.ARM)
                }
                CommandButton(Icons.Default.FlightTakeoff, "Takeoff", DroneXColors.Success, Modifier.weight(1f)) {
                    onCommand(DroneCommand.TAKEOFF)
                }
                CommandButton(Icons.Default.PauseCircle, "Hover", DroneXColors.Info, Modifier.weight(1f)) {
                    onCommand(DroneCommand.HOVER)
                }
                CommandButton(Icons.Default.FlightLand, "Land", DroneXColors.Warning, Modifier.weight(1f)) {
                    onCommand(DroneCommand.LAND)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                CommandButton(Icons.Default.Home, "RTL", DroneXColors.InfoBlue, Modifier.weight(1f)) {
                    onCommand(DroneCommand.RETURN_HOME)
                }
                CommandButton(Icons.Default.Memory, "Calibrate", DroneXColors.GreenAccent, Modifier.weight(1f)) {
                    onCommand(DroneCommand.CALIBRATE)
                }
                CommandButton(
                    icon = if (droneState.isRecording) Icons.Default.StopCircle else Icons.Default.PlayCircle,
                    label = if (droneState.isRecording) "Stop Rec" else "Record",
                    color = DroneXColors.PinkAccent,
                    modifier = Modifier.weight(1f)
                ) {
                    onCommand(if (droneState.isRecording) DroneCommand.STOP_RECORDING else DroneCommand.START_RECORDING)
                }
                CommandButton(Icons.Default.Warning, "E-Stop", DroneXColors.Critical, Modifier.weight(1f)) {
                    onCommand(DroneCommand.EMERGENCY_STOP)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                NavButton(Icons.Default.Gamepad, "Pilot", Modifier.weight(1f), onNavigateToControl)
                NavButton(Icons.Default.Map, "Map", Modifier.weight(1f), onNavigateToMap)
                NavButton(Icons.Default.Analytics, "Data", Modifier.weight(1f), onNavigateToAnalytics)
            }
        }
    }
}

@Composable
private fun CommandButton(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.16f), contentColor = color)
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
    }
}

@Composable
private fun NavButton(icon: ImageVector, label: String, modifier: Modifier, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = modifier.height(46.dp), shape = RoundedCornerShape(8.dp)) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(16.dp), tint = DroneXColors.PrimaryAccent)
        Spacer(Modifier.width(6.dp))
        Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ConnectionPanel(
    selectedMode: ConnectionMode,
    onModeChange: (ConnectionMode) -> Unit,
    ipAddress: String,
    onIpChange: (String) -> Unit,
    portText: String,
    onPortChange: (String) -> Unit,
    bluetoothAddress: String,
    onBluetoothChange: (String) -> Unit,
    droneState: DroneControlState,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    Surface(color = DroneXColors.SurfaceDark.copy(alpha = 0.92f), shape = RoundedCornerShape(10.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Wifi, contentDescription = null, tint = DroneXColors.PrimaryAccent)
                Spacer(Modifier.width(8.dp))
                Text("Hardware Link", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                ConnectionMode.values().forEach { mode ->
                    FilterChip(
                        selected = selectedMode == mode,
                        onClick = { onModeChange(mode) },
                        label = { Text(mode.label(), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        leadingIcon = {
                            Icon(
                                mode.icon(),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            if (selectedMode == ConnectionMode.ESP32_WIFI) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    LinkTextField(
                        label = "ESP32 IP",
                        value = ipAddress,
                        onValueChange = onIpChange,
                        modifier = Modifier.weight(1f)
                    )
                    LinkTextField(
                        label = "Port",
                        value = portText,
                        onValueChange = onPortChange,
                        modifier = Modifier.width(92.dp)
                    )
                }
            }

            if (selectedMode == ConnectionMode.BLUETOOTH) {
                LinkTextField(
                    label = "Bluetooth MAC",
                    value = bluetoothAddress,
                    onValueChange = onBluetoothChange,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            droneState.connectionError?.let {
                Text(it, color = DroneXColors.Critical, fontSize = 11.sp)
            }

            LinearProgressIndicator(
                progress = { if (droneState.droneState == DroneState.DISCONNECTED) 0f else droneState.signalStrength / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = DroneXColors.PrimaryAccent,
                trackColor = Color.White.copy(alpha = 0.08f)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onConnect,
                    modifier = Modifier.weight(1f).height(46.dp),
                    enabled = !droneState.isConnecting,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DroneXColors.PrimaryAccent)
                ) {
                    Text(if (droneState.isConnecting) "Connecting" else "Connect", color = Color.Black, fontWeight = FontWeight.Black)
                }
                TextButton(
                    onClick = onDisconnect,
                    modifier = Modifier.height(46.dp),
                    enabled = droneState.droneState != DroneState.DISCONNECTED
                ) {
                    Text("Disconnect", color = DroneXColors.Warning, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun EngineerPreflightPanel(
    droneState: DroneControlState,
    telemetry: Telemetry?,
    alertState: AlertState,
    aiState: AIState
) {
    val checks = listOf(
        "LINK" to (droneState.droneState != DroneState.DISCONNECTED && droneState.droneState != DroneState.ERROR),
        "BAT" to (((telemetry?.batteryVoltage ?: 12.6) / 12.6) > 0.28),
        "GPS" to ((telemetry?.gpsSatellites ?: 0) >= 8),
        "IMU" to (abs(telemetry?.pitch ?: 0.0) < 18.0 && abs(telemetry?.roll ?: 0.0) < 18.0),
        "AI" to (aiState.riskScore < 60.0),
        "ALRT" to (alertState.criticalCount == 0)
    )
    val score = checks.count { it.second } / checks.size.toFloat()
    val animatedScore by animateFloatAsState(
        targetValue = score,
        animationSpec = tween(600),
        label = "preflight"
    )

    Surface(color = DroneXColors.SurfaceDark.copy(alpha = 0.92f), shape = RoundedCornerShape(10.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FactCheck, contentDescription = null, tint = if (score == 1f) DroneXColors.Success else DroneXColors.Warning)
                    Spacer(Modifier.width(8.dp))
                    Text("Preflight Gate", color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
                }
                Text(
                    if (score == 1f) "GO" else "HOLD",
                    color = if (score == 1f) DroneXColors.Success else DroneXColors.Warning,
                    fontWeight = FontWeight.Black
                )
            }

            LinearProgressIndicator(
                progress = { animatedScore },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(6.dp)),
                color = if (score == 1f) DroneXColors.Success else DroneXColors.Warning,
                trackColor = Color.White.copy(alpha = 0.08f)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                checks.forEach { check ->
                    PreflightChip(label = check.first, ok = check.second, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PreflightChip(label: String, ok: Boolean, modifier: Modifier = Modifier) {
    Surface(
        color = if (ok) DroneXColors.Success.copy(alpha = 0.14f) else DroneXColors.Warning.copy(alpha = 0.14f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.height(32.dp)
    ) {
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (ok) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = if (ok) DroneXColors.Success else DroneXColors.Warning,
                modifier = Modifier.size(13.dp)
            )
            Spacer(Modifier.width(3.dp))
            Text(label, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black, maxLines = 1)
        }
    }
}

@Composable
private fun BlackboxPanel(
    telemetryState: TelemetryState,
    commandHistory: List<CommandLogItem>
) {
    Surface(color = DroneXColors.SurfaceDark.copy(alpha = 0.92f), shape = RoundedCornerShape(10.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DataUsage, contentDescription = null, tint = DroneXColors.GreenAccent)
                Spacer(Modifier.width(8.dp))
                Text("Mission Blackbox", color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                BlackboxMetric(Icons.Default.Timer, "Time", telemetryState.missionDurationMs.formatDuration(), Modifier.weight(1f))
                BlackboxMetric(Icons.Default.Timeline, "Rate", "${telemetryState.dataRateHz.format1()} Hz", Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                BlackboxMetric(Icons.Default.Route, "Dist", "${telemetryState.distanceEstimateMeters.roundToInt()} m", Modifier.weight(1f))
                BlackboxMetric(Icons.Default.VerticalAlignTop, "V/S", "${telemetryState.verticalSpeed.format1()} m/s", Modifier.weight(1f))
            }

            AnimatedVisibility(visible = commandHistory.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("Command Trace", color = Color.White.copy(alpha = 0.62f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    commandHistory.take(3).forEach { item ->
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(item.label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text(if (item.accepted) "OK" else "FAIL", color = if (item.accepted) DroneXColors.Success else DroneXColors.Critical, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BlackboxMetric(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Surface(color = Color.Black.copy(alpha = 0.18f), shape = RoundedCornerShape(8.dp), modifier = modifier.height(48.dp)) {
        Row(modifier = Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = DroneXColors.PrimaryAccent, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Column {
                Text(label, color = Color.White.copy(alpha = 0.52f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black, maxLines = 1)
            }
        }
    }
}

@Composable
private fun LinkTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = modifier,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Black.copy(alpha = 0.18f),
            unfocusedContainerColor = Color.Black.copy(alpha = 0.12f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedLabelColor = DroneXColors.PrimaryAccent,
            unfocusedLabelColor = Color.White.copy(alpha = 0.58f),
            focusedIndicatorColor = DroneXColors.PrimaryAccent,
            unfocusedIndicatorColor = Color.White.copy(alpha = 0.18f)
        )
    )
}

@Composable
private fun LiveAnalysisPanel(
    aiState: AIState,
    alertState: AlertState,
    modifier: Modifier = Modifier
) {
    Surface(
        color = DroneXColors.SurfaceDark.copy(alpha = 0.92f),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = DroneXColors.PinkAccent)
                Spacer(Modifier.width(8.dp))
                Text("On-device Analysis", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
            }

            RiskGauge(aiState.riskScore)

            Text("Recommendations", color = Color.White.copy(alpha = 0.72f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            aiState.recommendations.take(4).forEach {
                Text("- $it", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, lineHeight = 15.sp)
            }

            Text("Latest Alerts", color = Color.White.copy(alpha = 0.72f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            if (alertState.recentAlerts.isEmpty()) {
                Text("No active safety alert.", color = DroneXColors.Success, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            } else {
                alertState.recentAlerts.take(4).forEach { alert ->
                    Text(
                        "${alert.severity.name}: ${alert.title}",
                        color = if (alert.severity == AlertSeverity.CRITICAL) DroneXColors.Critical else DroneXColors.Warning,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun RiskGauge(riskScore: Double) {
    val animatedRisk by animateFloatAsState(
        targetValue = (riskScore / 100.0).toFloat(),
        animationSpec = tween(500),
        label = "risk"
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Risk score", color = Color.White, fontWeight = FontWeight.Bold)
            Text("${riskScore.roundToInt()}/100", color = riskColor(riskScore), fontWeight = FontWeight.Black)
        }
        LinearProgressIndicator(
            progress = { animatedRisk },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(6.dp)),
            color = riskColor(riskScore),
            trackColor = Color.White.copy(alpha = 0.08f)
        )
    }
}

private fun ConnectionMode.label(): String = when (this) {
    ConnectionMode.SIMULATION -> "SIM"
    ConnectionMode.ESP32_WIFI -> "WIFI"
    ConnectionMode.BLUETOOTH -> "BT"
}

private fun ConnectionMode.icon(): ImageVector = when (this) {
    ConnectionMode.SIMULATION -> Icons.Default.Gamepad
    ConnectionMode.ESP32_WIFI -> Icons.Default.Wifi
    ConnectionMode.BLUETOOTH -> Icons.Default.Bluetooth
}

private fun Double.format1(): String = String.format("%.1f", this)

private fun Long.formatDuration(): String {
    val seconds = this / 1000
    val minutes = seconds / 60
    val remainder = seconds % 60
    return "%02d:%02d".format(minutes, remainder)
}

private fun batteryColor(level: Int): Color = when {
    level > 55 -> DroneXColors.Success
    level > 25 -> DroneXColors.Warning
    else -> DroneXColors.Critical
}

private fun riskColor(riskScore: Double): Color = when {
    riskScore < 35 -> DroneXColors.Success
    riskScore < 65 -> DroneXColors.Warning
    else -> DroneXColors.Critical
}

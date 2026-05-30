package com.example.dn_26.presentation.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.FlightLand
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dn_26.domain.model.DroneCommand
import com.example.dn_26.domain.model.Telemetry
import com.example.dn_26.presentation.ui.theme.DroneXColors
import com.example.dn_26.presentation.viewmodel.DroneControlState
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun AdvancedFlightControlScreen(
    droneState: DroneControlState,
    telemetry: Telemetry?,
    onNavigateBack: () -> Unit = {},
    onCommand: (DroneCommand) -> Unit = {},
    onEmergencyStop: () -> Unit = {},
    onUnlockSafety: () -> Unit = {},
    onFlightModeChange: (String) -> Unit = {},
    onControllerPresetChange: (String) -> Unit = {},
    onControllerTuningChange: (Float, Float, Float) -> Unit = { _, _, _ -> },
    onSendJoystick: (Float, Float, Float, Float) -> Unit = { _, _, _, _ -> }
) {
    val modes = listOf("STABILIZE", "ALT HOLD", "LOITER", "RTL", "AUTO", "FOLLOW", "LAND")
    var selectedMode by remember { mutableStateOf(droneState.flightMode) }
    var leftOffset by remember { mutableStateOf(Offset.Zero) }
    var rightOffset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(droneState.flightMode) {
        selectedMode = droneState.flightMode
    }

    fun dispatchJoystick(newLeft: Offset = leftOffset, newRight: Offset = rightOffset) {
        leftOffset = newLeft
        rightOffset = newRight
        onSendJoystick(
            newRight.x,
            newRight.y,
            newLeft.y,
            newLeft.x
        )
    }

    Box(
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
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            PilotHeader(
                droneState = droneState,
                selectedMode = selectedMode,
                onBackClick = onNavigateBack,
                onEmergencyStop = onEmergencyStop,
                onUnlockSafety = onUnlockSafety
            )

            ScrollableTabRow(
                selectedTabIndex = modes.indexOf(selectedMode).coerceAtLeast(0),
                containerColor = Color.Transparent,
                contentColor = DroneXColors.PrimaryAccent,
                edgePadding = 16.dp,
                divider = {}
            ) {
                modes.forEach { mode ->
                    Tab(
                        selected = selectedMode == mode,
                        onClick = {
                            selectedMode = mode
                            onFlightModeChange(mode)
                            if (mode == "RTL") onCommand(DroneCommand.RETURN_HOME)
                            if (mode == "LAND") onCommand(DroneCommand.LAND)
                        },
                        text = {
                            Text(
                                mode,
                                color = if (selectedMode == mode) DroneXColors.PrimaryAccent else Color.White.copy(alpha = 0.45f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }

            MiniTelemetryBar(telemetry = telemetry, droneState = droneState)

            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PilotSidePanel(
                    telemetry = telemetry,
                    droneState = droneState,
                    onCommand = onCommand,
                    onControllerPresetChange = onControllerPresetChange,
                    onControllerTuningChange = onControllerTuningChange,
                    modifier = Modifier
                        .width(210.dp)
                        .fillMaxHeight()
                )

                VirtualJoystick(
                    label = "Throttle / Yaw",
                    color = DroneXColors.Warning,
                    value = leftOffset,
                    onOffsetChanged = { dispatchJoystick(newLeft = it) }
                )

                FlightAttitudeInstrument(
                    telemetry = telemetry,
                    modifier = Modifier.size(230.dp)
                )

                VirtualJoystick(
                    label = "Pitch / Roll",
                    color = DroneXColors.PrimaryAccent,
                    value = rightOffset,
                    onOffsetChanged = { dispatchJoystick(newRight = it) }
                )

                PilotSidePanelRight(
                    droneState = droneState,
                    onCommand = onCommand,
                    modifier = Modifier
                        .width(210.dp)
                        .fillMaxHeight()
                )
            }

            PilotBottomActions(
                droneState = droneState,
                onCommand = onCommand
            )
        }

        if (droneState.safetyLock) {
            EmergencyOverlay(onUnlockSafety)
        }
    }
}

@Composable
private fun PilotHeader(
    droneState: DroneControlState,
    selectedMode: String,
    onBackClick: () -> Unit,
    onEmergencyStop: () -> Unit,
    onUnlockSafety: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Column {
                Text("Pilot Command", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text(
                    "${droneState.droneState.name} | $selectedMode | ${droneState.connectionProfile.mode.name}",
                    color = Color.White.copy(alpha = 0.58f),
                    fontSize = 11.sp
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = onUnlockSafety,
                enabled = droneState.safetyLock,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DroneXColors.Success.copy(alpha = 0.18f),
                    contentColor = DroneXColors.Success
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("UNLOCK", fontWeight = FontWeight.Black)
            }
            Button(
                onClick = onEmergencyStop,
                colors = ButtonDefaults.buttonColors(containerColor = DroneXColors.Critical),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("E-STOP", fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun PilotSidePanel(
    telemetry: Telemetry?,
    droneState: DroneControlState,
    onCommand: (DroneCommand) -> Unit,
    onControllerPresetChange: (String) -> Unit,
    onControllerTuningChange: (Float, Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(color = DroneXColors.SurfaceDark.copy(alpha = 0.9f), shape = RoundedCornerShape(10.dp), modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Motor Stack", color = Color.White, fontWeight = FontWeight.Black)
            MotorBar("M1", telemetry?.speed?.toFloat() ?: 0f, DroneXColors.PrimaryAccent)
            MotorBar("M2", (telemetry?.speed?.toFloat() ?: 0f) * 0.92f, DroneXColors.GreenAccent)
            MotorBar("M3", (telemetry?.speed?.toFloat() ?: 0f) * 1.05f, DroneXColors.Warning)
            MotorBar("M4", (telemetry?.speed?.toFloat() ?: 0f) * 0.98f, DroneXColors.Info)

            Spacer(Modifier.height(8.dp))
            PilotAction(
                icon = Icons.Default.Shield,
                label = if (droneState.isArmed) "Disarm Motors" else "Arm Motors",
                color = DroneXColors.PrimaryAccent
            ) {
                onCommand(if (droneState.isArmed) DroneCommand.DISARM else DroneCommand.ARM)
            }
            PilotAction(Icons.Default.Sync, "Calibrate IMU", DroneXColors.GreenAccent) {
                onCommand(DroneCommand.CALIBRATE)
            }

            ControllerTuningPanel(
                droneState = droneState,
                onPresetChange = onControllerPresetChange,
                onTuningChange = onControllerTuningChange
            )
        }
    }
}

@Composable
private fun PilotSidePanelRight(
    droneState: DroneControlState,
    onCommand: (DroneCommand) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(color = DroneXColors.SurfaceDark.copy(alpha = 0.9f), shape = RoundedCornerShape(10.dp), modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Mission Actions", color = Color.White, fontWeight = FontWeight.Black)
            PilotAction(Icons.Default.FlightTakeoff, "Takeoff", DroneXColors.Success) {
                onCommand(DroneCommand.TAKEOFF)
            }
            PilotAction(Icons.Default.PauseCircle, "Hover Hold", DroneXColors.Info) {
                onCommand(DroneCommand.HOVER)
            }
            PilotAction(Icons.Default.Home, "Return Home", DroneXColors.InfoBlue) {
                onCommand(DroneCommand.RETURN_HOME)
            }
            PilotAction(Icons.Default.FlightLand, "Land", DroneXColors.Warning) {
                onCommand(DroneCommand.LAND)
            }
            PilotAction(
                icon = if (droneState.isRecording) Icons.Default.StopCircle else Icons.Default.PlayCircle,
                label = if (droneState.isRecording) "Stop Recording" else "Start Recording",
                color = DroneXColors.PinkAccent
            ) {
                onCommand(if (droneState.isRecording) DroneCommand.STOP_RECORDING else DroneCommand.START_RECORDING)
            }
        }
    }
}

@Composable
private fun ControllerTuningPanel(
    droneState: DroneControlState,
    onPresetChange: (String) -> Unit,
    onTuningChange: (Float, Float, Float) -> Unit
) {
    val tuning = droneState.controllerTuning
    val presets = listOf("PRECISION", "SPORT", "CINEMA", "INSPECT")

    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text("Controller Profile", color = Color.White.copy(alpha = 0.72f), fontSize = 11.sp, fontWeight = FontWeight.Black)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
            presets.forEach { preset ->
                FilterChip(
                    selected = tuning.preset == preset,
                    onClick = { onPresetChange(preset) },
                    label = { Text(preset.take(3), fontSize = 8.sp, fontWeight = FontWeight.Black) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        CompactSlider(
            label = "SENS",
            value = tuning.sensitivity,
            range = 0.2f..1.25f,
            color = DroneXColors.PrimaryAccent,
            onValueChange = { onTuningChange(it, tuning.deadZone, tuning.expo) }
        )
        CompactSlider(
            label = "DZ",
            value = tuning.deadZone,
            range = 0.02f..0.25f,
            color = DroneXColors.Warning,
            onValueChange = { onTuningChange(tuning.sensitivity, it, tuning.expo) }
        )
        CompactSlider(
            label = "EXPO",
            value = tuning.expo,
            range = 0f..0.8f,
            color = DroneXColors.GreenAccent,
            onValueChange = { onTuningChange(tuning.sensitivity, tuning.deadZone, it) }
        )

        StickSignalMeter(droneState = droneState)
    }
}

@Composable
private fun CompactSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    color: Color,
    onValueChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(label, color = Color.White.copy(alpha = 0.58f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text(String.format("%.2f", value), color = color, fontSize = 9.sp, fontWeight = FontWeight.Black)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = Color.White.copy(alpha = 0.12f)
            ),
            modifier = Modifier.height(24.dp)
        )
    }
}

@Composable
private fun StickSignalMeter(droneState: DroneControlState) {
    val signal = droneState.lastStickSignal
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        SignalAxis("P", signal.pitch, DroneXColors.PrimaryAccent)
        SignalAxis("R", signal.roll, DroneXColors.GreenAccent)
        SignalAxis("T", signal.throttle, DroneXColors.Warning)
        SignalAxis("Y", signal.yaw, DroneXColors.PinkAccent)
    }
}

@Composable
private fun SignalAxis(label: String, value: Float, color: Color) {
    val animatedValue by animateFloatAsState(value.coerceIn(-1.25f, 1.25f), animationSpec = tween(140), label = "axis")
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
        Text(label, color = color, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth((kotlin.math.abs(animatedValue) / 1.25f).coerceIn(0f, 1f))
                    .align(if (animatedValue >= 0f) Alignment.CenterStart else Alignment.CenterEnd)
                    .background(color, RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
private fun PilotAction(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(44.dp),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.14f), contentColor = color)
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun MotorBar(label: String, value: Float, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("${(900 + value * 45).roundToInt()} rpm", color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        androidx.compose.material3.LinearProgressIndicator(
            progress = { ((value / 28f) + 0.12f).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(5.dp),
            color = color,
            trackColor = Color.White.copy(alpha = 0.08f)
        )
    }
}

@Composable
private fun VirtualJoystick(
    label: String,
    color: Color,
    value: Offset,
    onOffsetChanged: (Offset) -> Unit
) {
    var thumbOffset by remember { mutableStateOf(Offset.Zero) }
    val radius = 94.dp
    val stickIntensity = sqrt(thumbOffset.x.pow(2) + thumbOffset.y.pow(2)) / 94f
    val animatedThumb by animateFloatAsState(
        targetValue = (62f + stickIntensity.coerceIn(0f, 1f) * 10f),
        animationSpec = tween(120),
        label = "thumb_size"
    )

    LaunchedEffect(value) {
        if (value == Offset.Zero) thumbOffset = Offset.Zero
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(label.uppercase(), color = color, fontSize = 11.sp, fontWeight = FontWeight.Black)
        Box(
            modifier = Modifier
                .size(radius * 2)
                .background(Color.Black.copy(alpha = 0.28f), CircleShape)
                .border(1.dp, color.copy(alpha = 0.45f), CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            thumbOffset = Offset.Zero
                            onOffsetChanged(Offset.Zero)
                        },
                        onDragCancel = {
                            thumbOffset = Offset.Zero
                            onOffsetChanged(Offset.Zero)
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val next = thumbOffset + dragAmount
                            val distance = sqrt(next.x.pow(2) + next.y.pow(2))
                            val maxDistance = radius.toPx()
                            thumbOffset = if (distance <= maxDistance) {
                                next
                            } else {
                                Offset(next.x / distance * maxDistance, next.y / distance * maxDistance)
                            }
                            onOffsetChanged(Offset(thumbOffset.x / maxDistance, -thumbOffset.y / maxDistance))
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                drawCircle(color.copy(alpha = 0.08f), radius = size.minDimension / 2.5f, center = center, style = Stroke(1.dp.toPx()))
                drawLine(Color.White.copy(alpha = 0.12f), Offset(0f, center.y), Offset(size.width, center.y), strokeWidth = 1.dp.toPx())
                drawLine(Color.White.copy(alpha = 0.12f), Offset(center.x, 0f), Offset(center.x, size.height), strokeWidth = 1.dp.toPx())
            }

            Box(
                modifier = Modifier
                    .offset { IntOffset(thumbOffset.x.toInt(), thumbOffset.y.toInt()) }
                    .size(animatedThumb.dp)
                    .background(
                        brush = Brush.radialGradient(listOf(color, color.copy(alpha = 0.42f))),
                        shape = CircleShape
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
            )
        }
    }
}

@Composable
private fun FlightAttitudeInstrument(
    telemetry: Telemetry?,
    modifier: Modifier = Modifier
) {
    val pitch = telemetry?.pitch ?: 0.0
    val roll = telemetry?.roll ?: 0.0
    val yaw = telemetry?.yaw ?: 0.0

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2
            drawCircle(Color.Black.copy(alpha = 0.28f), radius, center)
            drawCircle(DroneXColors.PrimaryAccent.copy(alpha = 0.2f), radius - 2.dp.toPx(), center, style = Stroke(1.dp.toPx()))

            for (angle in 0 until 360 step 30) {
                val rad = (angle - 90) * PI / 180.0
                val outer = Offset(center.x + cos(rad).toFloat() * radius * 0.92f, center.y + sin(rad).toFloat() * radius * 0.92f)
                val inner = Offset(center.x + cos(rad).toFloat() * radius * 0.78f, center.y + sin(rad).toFloat() * radius * 0.78f)
                drawLine(Color.White.copy(alpha = 0.2f), inner, outer, strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
            }

            val rollOffset = (roll / 45.0).toFloat().coerceIn(-1f, 1f) * radius * 0.25f
            val pitchOffset = (pitch / 45.0).toFloat().coerceIn(-1f, 1f) * radius * 0.42f
            drawLine(
                DroneXColors.Warning,
                Offset(center.x - radius * 0.7f, center.y + pitchOffset + rollOffset),
                Offset(center.x + radius * 0.7f, center.y + pitchOffset - rollOffset),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawCircle(DroneXColors.PrimaryAccent, 5.dp.toPx(), center)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("HEADING", color = Color.White.copy(alpha = 0.45f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text("${yaw.roundToInt()} deg", color = Color.White, fontWeight = FontWeight.Black, fontSize = 26.sp)
            Text("P ${pitch.roundToInt()}  R ${roll.roundToInt()}", color = DroneXColors.PrimaryAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MiniTelemetryBar(
    telemetry: Telemetry?,
    droneState: DroneControlState
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(DroneXColors.SurfaceDark.copy(alpha = 0.72f), RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TelemetryItem(Icons.Default.VerticalAlignTop, "${telemetry?.altitude?.roundToInt() ?: 0} m")
        TelemetryItem(Icons.Default.Speed, "${telemetry?.speed?.let { (it * 3.6).roundToInt() } ?: 0} km/h")
        TelemetryItem(Icons.Default.Radar, "${telemetry?.yaw?.roundToInt() ?: 0} deg")
        TelemetryItem(Icons.Default.GpsFixed, "${telemetry?.gpsSatellites ?: 0} sats")
        TelemetryItem(Icons.Default.BatteryFull, "${droneState.batteryLevel}%")
        TelemetryItem(Icons.Default.Tune, droneState.connectionQuality)
    }
}

@Composable
private fun TelemetryItem(icon: ImageVector, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = DroneXColors.PrimaryAccent, modifier = Modifier.size(15.dp))
        Spacer(modifier = Modifier.width(5.dp))
        Text(value, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PilotBottomActions(
    droneState: DroneControlState,
    onCommand: (DroneCommand) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        BottomCommand(Icons.Default.FlightTakeoff, "TAKEOFF", DroneXColors.Success, Modifier.weight(1f)) {
            onCommand(DroneCommand.TAKEOFF)
        }
        BottomCommand(Icons.Default.PauseCircle, "HOVER", DroneXColors.Info, Modifier.weight(1f)) {
            onCommand(DroneCommand.HOVER)
        }
        BottomCommand(Icons.Default.Home, "RTL", DroneXColors.InfoBlue, Modifier.weight(1f)) {
            onCommand(DroneCommand.RETURN_HOME)
        }
        BottomCommand(Icons.Default.FlightLand, "LAND", DroneXColors.Warning, Modifier.weight(1f)) {
            onCommand(DroneCommand.LAND)
        }
        BottomCommand(
            icon = if (droneState.isRecording) Icons.Default.StopCircle else Icons.Default.PlayCircle,
            label = if (droneState.isRecording) "STOP REC" else "RECORD",
            color = DroneXColors.PinkAccent,
            modifier = Modifier.weight(1f)
        ) {
            onCommand(if (droneState.isRecording) DroneCommand.STOP_RECORDING else DroneCommand.START_RECORDING)
        }
    }
}

@Composable
private fun BottomCommand(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.14f), contentColor = color),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(17.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, fontWeight = FontWeight.Black, fontSize = 11.sp, maxLines = 1)
    }
}

@Composable
private fun EmergencyOverlay(onUnlockSafety: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.76f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = DroneXColors.Critical.copy(alpha = 0.96f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(72.dp))
                Text("MOTORS STOPPED", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black)
                Text("Safety lock is active. Disarm before resuming.", color = Color.White.copy(alpha = 0.82f), fontSize = 13.sp)
                Button(
                    onClick = onUnlockSafety,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = DroneXColors.Critical)
                ) {
                    Text("UNLOCK AND DISARM", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

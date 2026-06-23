package com.example.dn_26.presentation.ui.screen

import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.dn_26.domain.model.ConnectionMode
import com.example.dn_26.domain.model.ConnectionProfile
import com.example.dn_26.domain.model.DroneCommand
import com.example.dn_26.domain.model.Telemetry
import com.example.dn_26.presentation.ui.theme.DroneXColors
import com.example.dn_26.presentation.viewmodel.DroneControlState
import kotlin.math.pow
import kotlin.math.roundToInt
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
    val modes = listOf("PRECISION", "SPORT", "CINEMA", "INSPECT")
    var selectedMode by remember { mutableStateOf(droneState.controllerTuning.preset) }
    var leftStick by remember { mutableStateOf(Offset.Zero) }
    var rightStick by remember { mutableStateOf(Offset.Zero) }
    val streamUrl = remember(droneState.connectionProfile) {
        droneState.connectionProfile.defaultPilotStreamUrl()
    }

    LaunchedEffect(droneState.controllerTuning.preset) {
        selectedMode = droneState.controllerTuning.preset
    }

    fun dispatch(newLeft: Offset = leftStick, newRight: Offset = rightStick) {
        leftStick = newLeft
        rightStick = newRight
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
            .background(PilotStageBrush)
    ) {
        PilotFpvView(streamUrl = streamUrl, modifier = Modifier.fillMaxSize())

        PilotTopHud(
            droneState = droneState,
            telemetry = telemetry,
            selectedMode = selectedMode,
            modes = modes,
            onModeSelected = { mode ->
                selectedMode = mode
                onControllerPresetChange(mode)
                onFlightModeChange(
                    when (mode) {
                        "SPORT" -> "SPORT"
                        "CINEMA" -> "CINEMA"
                        "INSPECT" -> "LOITER"
                        else -> "STABILIZE"
                    }
                )
            },
            onBack = onNavigateBack,
            onEmergencyStop = onEmergencyStop
        )

        GameLeftCluster(
            value = leftStick,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 26.dp, bottom = 28.dp),
            onStick = { dispatch(newLeft = it) },
            onL1 = { onCommand(if (droneState.isArmed) DroneCommand.DISARM else DroneCommand.ARM) },
            onL2 = { onCommand(DroneCommand.CALIBRATE) }
        )

        GameRightCluster(
            value = rightStick,
            isRecording = droneState.isRecording,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 26.dp, bottom = 28.dp),
            onStick = { dispatch(newRight = it) },
            onButton = { label ->
                when (label) {
                    "A" -> onCommand(DroneCommand.TAKEOFF)
                    "B" -> onCommand(DroneCommand.LAND)
                    "X" -> onCommand(DroneCommand.HOVER)
                    "Y" -> onCommand(DroneCommand.RETURN_HOME)
                    "REC" -> onCommand(if (droneState.isRecording) DroneCommand.STOP_RECORDING else DroneCommand.START_RECORDING)
                }
            }
        )

        PilotBottomStrip(
            droneState = droneState,
            telemetry = telemetry,
            leftStick = leftStick,
            rightStick = rightStick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 18.dp)
        )

        if (droneState.safetyLock) {
            SafetyOverlay(onUnlockSafety)
        }
    }
}

@Composable
private fun PilotFpvView(streamUrl: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.background(PilotStageBrush), contentAlignment = Alignment.Center) {
        if (streamUrl.isBlank()) {
            Surface(
                color = PilotPanel.copy(alpha = 0.82f),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, PilotStroke.copy(alpha = 0.7f))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Videocam, contentDescription = null, tint = Color(0xFFFFB74D), modifier = Modifier.size(54.dp))
                    Text("FPV OFFLINE", color = PilotText, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Text("Connect WiFi ESP32-CAM stream", color = PilotMuted, fontSize = 12.sp)
                }
            }
        } else {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        setBackgroundColor(android.graphics.Color.BLACK)
                        webViewClient = WebViewClient()
                        settings.javaScriptEnabled = false
                        settings.domStorageEnabled = false
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        loadUrl(streamUrl)
                    }
                },
                update = { webView ->
                    if (webView.url != streamUrl) webView.loadUrl(streamUrl)
                }
            )
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            drawCircle(Color.White.copy(alpha = 0.2f), 4.dp.toPx(), center)
            drawLine(
                Color.White.copy(alpha = 0.2f),
                Offset(center.x - 42.dp.toPx(), center.y),
                Offset(center.x - 12.dp.toPx(), center.y),
                strokeWidth = 1.4.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                Color.White.copy(alpha = 0.2f),
                Offset(center.x + 12.dp.toPx(), center.y),
                Offset(center.x + 42.dp.toPx(), center.y),
                strokeWidth = 1.4.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun PilotTopHud(
    droneState: DroneControlState,
    telemetry: Telemetry?,
    selectedMode: String,
    modes: List<String>,
    onModeSelected: (String) -> Unit,
    onBack: () -> Unit,
    onEmergencyStop: () -> Unit
) {
    Surface(
        color = PilotPanel.copy(alpha = 0.88f),
        shape = RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, PilotStroke.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PilotText)
                }
                HudPill(Icons.Default.Videocam, droneState.droneState.name)
                HudPill(Icons.Default.Speed, "${telemetry?.speed?.let { (it * 3.6).roundToInt() } ?: 0} km/h")
                HudPill(Icons.Default.GpsFixed, "${telemetry?.gpsSatellites ?: 0} SAT")
                HudPill(Icons.Default.BatteryFull, "${droneState.batteryLevel}%")
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                modes.forEach { mode ->
                    FilterChip(
                        selected = selectedMode == mode,
                        onClick = { onModeSelected(mode) },
                        label = { Text(mode.take(4), fontSize = 10.sp, fontWeight = FontWeight.Black) }
                    )
                }
                Button(
                    onClick = onEmergencyStop,
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DroneXColors.Critical)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("STOP", fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun HudPill(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .background(PilotPanelHigh.copy(alpha = 0.9f), RoundedCornerShape(18.dp))
            .border(1.dp, PilotStroke.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
            .padding(horizontal = 9.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFFFFB74D), modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(5.dp))
        Text(text, color = PilotText, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun GameLeftCluster(
    value: Offset,
    modifier: Modifier,
    onStick: (Offset) -> Unit,
    onL1: () -> Unit,
    onL2: () -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ShoulderButton("L1", DroneXColors.PrimaryAccent, onL1)
            ShoulderButton("L2", DroneXColors.Warning, onL2)
        }
        GameStick(
            label = "MOVE",
            color = DroneXColors.PrimaryAccent,
            value = value,
            onChange = onStick
        )
    }
}

@Composable
private fun GameRightCluster(
    value: Offset,
    isRecording: Boolean,
    modifier: Modifier,
    onStick: (Offset) -> Unit,
    onButton: (String) -> Unit
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(18.dp), verticalAlignment = Alignment.Bottom) {
        GameStick(
            label = "CAM",
            color = DroneXColors.GreenAccent,
            value = value,
            onChange = onStick
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            GameButton("Y", "RTL", Color(0xFFFACC15)) { onButton("Y") }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GameButton("X", "HVR", Color(0xFF60A5FA)) { onButton("X") }
                GameButton("B", "LAND", Color(0xFFF87171)) { onButton("B") }
            }
            GameButton("A", "UP", Color(0xFF34D399)) { onButton("A") }
            GameButton(if (isRecording) "ON" else "REC", if (isRecording) "STOP" else "START", if (isRecording) DroneXColors.Critical else DroneXColors.PinkAccent) { onButton("REC") }
        }
    }
}

@Composable
private fun GameStick(
    label: String,
    color: Color,
    value: Offset,
    onChange: (Offset) -> Unit
) {
    var thumb by remember { mutableStateOf(Offset.Zero) }
    val radius = 88.dp
    val pulse by animateFloatAsState(
        targetValue = if (value == Offset.Zero) 58f else 70f,
        animationSpec = tween(120),
        label = "game_stick"
    )

    Box(
        modifier = Modifier
            .size(radius * 2)
            .background(PilotPanel.copy(alpha = 0.45f), CircleShape)
            .border(2.dp, color.copy(alpha = 0.6f), CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        thumb = Offset.Zero
                        onChange(Offset.Zero)
                    },
                    onDragCancel = {
                        thumb = Offset.Zero
                        onChange(Offset.Zero)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val next = thumb + dragAmount
                        val distance = sqrt(next.x.pow(2) + next.y.pow(2))
                        val maxDistance = radius.toPx()
                        thumb = if (distance <= maxDistance) {
                            next
                        } else {
                            Offset(next.x / distance * maxDistance, next.y / distance * maxDistance)
                        }
                        onChange(Offset(thumb.x / maxDistance, -thumb.y / maxDistance))
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            drawCircle(color.copy(alpha = 0.12f), radius = size.minDimension / 2.9f, center = center)
            drawLine(PilotText.copy(alpha = 0.2f), Offset(center.x, 0f), Offset(center.x, size.height), 1.dp.toPx())
            drawLine(PilotText.copy(alpha = 0.2f), Offset(0f, center.y), Offset(size.width, center.y), 1.dp.toPx())
        }
        Box(
            modifier = Modifier
                .offset { IntOffset(thumb.x.toInt(), thumb.y.toInt()) }
                .size(pulse.dp)
                .background(
                    brush = Brush.radialGradient(listOf(color, color.copy(alpha = 0.65f))),
                    shape = CircleShape
                )
                .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(label, color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun GameButton(label: String, subLabel: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(58.dp),
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.92f), contentColor = Color.Black)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 16.sp, fontWeight = FontWeight.Black, lineHeight = 16.sp)
            Text(subLabel, fontSize = 8.sp, fontWeight = FontWeight.Black, lineHeight = 8.sp)
        }
    }
}

@Composable
private fun ShoulderButton(label: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.width(72.dp).height(38.dp),
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PilotPanel.copy(alpha = 0.7f), contentColor = color)
    ) {
        Text(label, fontWeight = FontWeight.Black, fontSize = 13.sp)
    }
}

@Composable
private fun PilotBottomStrip(
    droneState: DroneControlState,
    telemetry: Telemetry?,
    leftStick: Offset,
    rightStick: Offset,
    modifier: Modifier = Modifier
) {
    Surface(color = PilotPanel.copy(alpha = 0.85f), shape = RoundedCornerShape(28.dp), modifier = modifier, border = androidx.compose.foundation.BorderStroke(1.dp, PilotStroke.copy(alpha = 0.3f))) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StripItem("ALT", "${telemetry?.altitude?.roundToInt() ?: 0} m")
            StripItem("L", "${leftStick.x.formatAxis()}, ${leftStick.y.formatAxis()}")
            StripItem("R", "${rightStick.x.formatAxis()}, ${rightStick.y.formatAxis()}")
            StripItem("REC", if (droneState.isRecording) "ON" else "OFF")
        }
    }
}

@Composable
private fun StripItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = PilotText.copy(alpha = 0.6f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Text(value, color = PilotText, fontSize = 11.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun SafetyOverlay(onUnlockSafety: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.82f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(color = DroneXColors.Critical, shape = RoundedCornerShape(14.dp)) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.StopCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(72.dp))
                Text("CONTROL LOCKED", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black)
                Button(
                    onClick = onUnlockSafety,
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = DroneXColors.Critical)
                ) {
                    Text("UNLOCK", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

private fun ConnectionProfile.defaultPilotStreamUrl(): String {
    return when (mode) {
        ConnectionMode.ESP32_WIFI -> "http://$ipAddress:81/stream"
        ConnectionMode.SIMULATION -> "http://$ipAddress:81/stream"
        ConnectionMode.BLUETOOTH -> ""
    }
}

private fun Float.formatAxis(): String = String.format("%.2f", this)

// Theme Colors for Pilot HUD
val PilotStageBrush = Brush.verticalGradient(
    listOf(Color(0xFF1C1F22), Color(0xFF282C31))
)
val PilotPanel = Color(0xFF373C41)
val PilotPanelHigh = Color(0xFF4A5057)
val PilotStroke = Color(0xFF5E656C)
val PilotText = Color(0xFFECEFF1)
val PilotMuted = Color(0xFFB0BEC5)

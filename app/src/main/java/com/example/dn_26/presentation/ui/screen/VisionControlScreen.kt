package com.example.dn_26.presentation.ui.screen

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FlightLand
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.example.dn_26.domain.model.AlertSeverity
import com.example.dn_26.domain.model.ConnectionMode
import com.example.dn_26.domain.model.DroneCommand
import com.example.dn_26.domain.model.Telemetry
import com.example.dn_26.presentation.ui.theme.DroneXColors
import com.example.dn_26.presentation.viewmodel.DroneControlState
import com.example.dn_26.presentation.viewmodel.VisionAIViewModel
import com.example.dn_26.presentation.viewmodel.VisionFinding
import kotlinx.coroutines.delay
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Composable
fun VisionControlScreen(
    droneState: DroneControlState,
    telemetry: Telemetry?,
    visionViewModel: VisionAIViewModel,
    onNavigateBack: () -> Unit,
    onCommand: (DroneCommand) -> Unit,
    onJoystick: (Float, Float, Float, Float) -> Unit
) {
    val visionState by visionViewModel.state.collectAsState()
    val defaultStreamUrl = droneState.connectionProfile.defaultStreamUrl()
    val defaultSnapshotUrl = droneState.connectionProfile.defaultSnapshotUrl()
    var streamUrl by remember(defaultStreamUrl) { mutableStateOf(defaultStreamUrl) }
    var snapshotUrl by remember(defaultSnapshotUrl) { mutableStateOf(defaultSnapshotUrl) }
    var question by remember { mutableStateOf("") }

    LaunchedEffect(defaultStreamUrl, defaultSnapshotUrl) {
        if (visionState.streamUrl.isBlank()) {
            visionViewModel.updateUrls(defaultStreamUrl, defaultSnapshotUrl)
        }
    }

    LaunchedEffect(visionState.autoAnalyze, visionState.snapshotUrl) {
        while (visionState.autoAnalyze) {
            visionViewModel.analyzeSnapshot()
            delay(2_500)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(DroneXColors.BackgroundDark, Color(0xFF101715), Color(0xFF17140F))
                )
            )
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1.55f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            VisionHeader(
                droneState = droneState,
                telemetry = telemetry,
                onNavigateBack = onNavigateBack
            )

            FpvVideoSurface(
                streamUrl = visionState.streamUrl.ifBlank { streamUrl },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            VisionUrlBar(
                streamUrl = streamUrl,
                snapshotUrl = snapshotUrl,
                onStreamChange = { streamUrl = it },
                onSnapshotChange = { snapshotUrl = it },
                onApply = { visionViewModel.updateUrls(streamUrl, snapshotUrl) },
                onRefreshFrame = { visionViewModel.analyzeSnapshot(snapshotUrl) }
            )

            FpvGamepadDeck(
                droneState = droneState,
                onCommand = onCommand,
                onJoystick = onJoystick
            )
        }

        Column(
            modifier = Modifier
                .width(380.dp)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            VisionRecorderPanel(
                isRecording = droneState.isRecording,
                isAnalyzing = visionState.isAnalyzing,
                autoAnalyze = visionState.autoAnalyze,
                onRecordToggle = {
                    onCommand(if (droneState.isRecording) DroneCommand.STOP_RECORDING else DroneCommand.START_RECORDING)
                },
                onAnalyze = { visionViewModel.analyzeSnapshot(snapshotUrl) },
                onAutoAnalyzeChange = visionViewModel::setAutoAnalyze
            )

            VisionAnalysisPanel(findings = visionState.findings, score = visionState.frameScore)

            VisionMetricsPanel(
                brightness = visionState.brightness,
                contrast = visionState.contrast,
                sharpness = visionState.sharpness,
                error = visionState.error
            )

            VisionQuestionPanel(
                question = question,
                onQuestionChange = { question = it },
                onSend = {
                    visionViewModel.askQuestion(question)
                    question = ""
                },
                history = visionState.qaHistory
            )
        }
    }
}

@Composable
private fun VisionHeader(
    droneState: DroneControlState,
    telemetry: Telemetry?,
    onNavigateBack: () -> Unit
) {
    Surface(color = DroneXColors.SurfaceDark.copy(alpha = 0.92f), shape = RoundedCornerShape(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Icon(Icons.Default.Videocam, contentDescription = null, tint = DroneXColors.PrimaryAccent)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("FPV Vision Control", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Text(
                        "${droneState.connectionProfile.mode.name} | ${droneState.droneState.name} | ${droneState.flightMode}",
                        color = Color.White.copy(alpha = 0.58f),
                        fontSize = 11.sp
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                FpvStatus(Icons.Default.Speed, "${telemetry?.speed?.let { (it * 3.6).roundToInt() } ?: 0} km/h")
                FpvStatus(Icons.Default.Route, "${telemetry?.altitude?.roundToInt() ?: 0} m")
                FpvStatus(Icons.Default.GpsFixed, "${telemetry?.gpsSatellites ?: 0} sats")
                FpvStatus(Icons.Default.BatteryFull, "${droneState.batteryLevel}%")
            }
        }
    }
}

@Composable
private fun FpvStatus(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = DroneXColors.PrimaryAccent, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FpvVideoSurface(streamUrl: String, modifier: Modifier = Modifier) {
    Surface(color = Color.Black, shape = RoundedCornerShape(10.dp), modifier = modifier) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (streamUrl.isBlank()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("NO FPV STREAM", color = Color.White.copy(alpha = 0.42f), fontWeight = FontWeight.Black)
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

            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("LIVE", color = DroneXColors.Success, fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun VisionUrlBar(
    streamUrl: String,
    snapshotUrl: String,
    onStreamChange: (String) -> Unit,
    onSnapshotChange: (String) -> Unit,
    onApply: () -> Unit,
    onRefreshFrame: () -> Unit
) {
    Surface(color = DroneXColors.SurfaceDark.copy(alpha = 0.9f), shape = RoundedCornerShape(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompactUrlField("Stream", streamUrl, onStreamChange, Modifier.weight(1.2f))
            CompactUrlField("Snapshot", snapshotUrl, onSnapshotChange, Modifier.weight(1f))
            Button(
                onClick = onApply,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DroneXColors.PrimaryAccent)
            ) {
                Text("APPLY", color = Color.Black, fontWeight = FontWeight.Black)
            }
            IconButton(onClick = onRefreshFrame) {
                Icon(Icons.Default.Refresh, contentDescription = "Analyze", tint = DroneXColors.PrimaryAccent)
            }
        }
    }
}

@Composable
private fun CompactUrlField(
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
        modifier = modifier.height(56.dp),
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
private fun FpvGamepadDeck(
    droneState: DroneControlState,
    onCommand: (DroneCommand) -> Unit,
    onJoystick: (Float, Float, Float, Float) -> Unit
) {
    var left by remember { mutableStateOf(Offset.Zero) }
    var right by remember { mutableStateOf(Offset.Zero) }

    fun dispatch(newLeft: Offset = left, newRight: Offset = right) {
        left = newLeft
        right = newRight
        onJoystick(newRight.x, newRight.y, newLeft.y, newLeft.x)
    }

    Surface(color = DroneXColors.SurfaceDark.copy(alpha = 0.9f), shape = RoundedCornerShape(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(170.dp).padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FpvStick("L", DroneXColors.Warning, left) { dispatch(newLeft = it) }

            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FpvCommandButton(Icons.Default.FlightTakeoff, "A", DroneXColors.Success) { onCommand(DroneCommand.TAKEOFF) }
                    FpvCommandButton(Icons.Default.PauseCircle, "X", DroneXColors.Info) { onCommand(DroneCommand.HOVER) }
                    FpvCommandButton(Icons.Default.Home, "Y", DroneXColors.InfoBlue) { onCommand(DroneCommand.RETURN_HOME) }
                    FpvCommandButton(Icons.Default.FlightLand, "B", DroneXColors.Warning) { onCommand(DroneCommand.LAND) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { onCommand(if (droneState.isArmed) DroneCommand.DISARM else DroneCommand.ARM) },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (droneState.isArmed) "DISARM" else "ARM", color = Color.White, fontWeight = FontWeight.Black)
                    }
                    Button(
                        onClick = { onCommand(DroneCommand.EMERGENCY_STOP) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DroneXColors.Critical)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("E-STOP", fontWeight = FontWeight.Black)
                    }
                }
            }

            FpvStick("R", DroneXColors.PrimaryAccent, right) { dispatch(newRight = it) }
        }
    }
}

@Composable
private fun FpvStick(label: String, color: Color, value: Offset, onChange: (Offset) -> Unit) {
    var thumb by remember { mutableStateOf(Offset.Zero) }
    val radius = 62.dp
    val pulse by animateFloatAsState(
        targetValue = if (value == Offset.Zero) 38f else 46f,
        animationSpec = tween(140),
        label = "fpv_stick"
    )

    Box(
        modifier = Modifier
            .size(radius * 2)
            .background(Color.Black.copy(alpha = 0.28f), CircleShape)
            .border(1.dp, color.copy(alpha = 0.5f), CircleShape)
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
            drawLine(Color.White.copy(alpha = 0.12f), Offset(center.x, 0f), Offset(center.x, size.height), 1.dp.toPx())
            drawLine(Color.White.copy(alpha = 0.12f), Offset(0f, center.y), Offset(size.width, center.y), 1.dp.toPx())
            drawCircle(color.copy(alpha = 0.12f), size.minDimension / 2.8f, center, style = Stroke(1.dp.toPx(), cap = StrokeCap.Round))
        }
        Box(
            modifier = Modifier
                .offset { IntOffset(thumb.x.toInt(), thumb.y.toInt()) }
                .size(pulse.dp)
                .background(color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(label, color = Color.Black, fontWeight = FontWeight.Black, fontSize = 13.sp)
        }
    }
}

@Composable
private fun FpvCommandButton(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.2f), contentColor = color),
        modifier = Modifier.size(48.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(17.dp))
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun VisionRecorderPanel(
    isRecording: Boolean,
    isAnalyzing: Boolean,
    autoAnalyze: Boolean,
    onRecordToggle: () -> Unit,
    onAnalyze: () -> Unit,
    onAutoAnalyzeChange: (Boolean) -> Unit
) {
    Surface(color = DroneXColors.SurfaceDark.copy(alpha = 0.92f), shape = RoundedCornerShape(10.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = DroneXColors.PrimaryAccent)
                    Spacer(Modifier.width(8.dp))
                    Text("Video Recorder", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
                FilterChip(
                    selected = isRecording,
                    onClick = onRecordToggle,
                    label = { Text(if (isRecording) "REC" else "IDLE", fontWeight = FontWeight.Black) },
                    leadingIcon = {
                        Icon(if (isRecording) Icons.Default.StopCircle else Icons.Default.PlayCircle, contentDescription = null)
                    }
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onRecordToggle,
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isRecording) DroneXColors.Critical else DroneXColors.PinkAccent)
                ) {
                    Text(if (isRecording) "STOP VIDEO" else "START VIDEO", fontWeight = FontWeight.Black)
                }
                OutlinedButton(
                    onClick = onAnalyze,
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = DroneXColors.PrimaryAccent, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (isAnalyzing) "ANALYZING" else "ANALYZE", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Auto AI frame scan", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Switch(checked = autoAnalyze, onCheckedChange = onAutoAnalyzeChange)
            }
        }
    }
}

@Composable
private fun VisionAnalysisPanel(findings: List<VisionFinding>, score: Int) {
    Surface(color = DroneXColors.SurfaceDark.copy(alpha = 0.92f), shape = RoundedCornerShape(10.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Vision AI Findings", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                Text("$score/100", color = scoreColor(score), fontWeight = FontWeight.Black)
            }
            LinearProgressIndicator(
                progress = { score / 100f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(6.dp)),
                color = scoreColor(score),
                trackColor = Color.White.copy(alpha = 0.08f)
            )
            if (findings.isEmpty()) {
                VisionFindingRow(
                    finding = VisionFinding(
                        title = "Nominal frame",
                        description = "No visual anomaly detected in the latest analyzed snapshot.",
                        severity = AlertSeverity.LOW,
                        confidence = 0.7
                    )
                )
            } else {
                findings.take(5).forEach { VisionFindingRow(it) }
            }
        }
    }
}

@Composable
private fun VisionFindingRow(finding: VisionFinding) {
    Surface(color = Color.Black.copy(alpha = 0.18f), shape = RoundedCornerShape(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.Top) {
            Icon(
                if (finding.severity == AlertSeverity.CRITICAL) Icons.Default.Warning else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = severityColor(finding.severity),
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(finding.title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                Text(finding.description, color = Color.White.copy(alpha = 0.68f), fontSize = 10.sp, lineHeight = 14.sp)
            }
        }
    }
}

@Composable
private fun VisionMetricsPanel(
    brightness: Double,
    contrast: Double,
    sharpness: Double,
    error: String?
) {
    Surface(color = DroneXColors.SurfaceDark.copy(alpha = 0.92f), shape = RoundedCornerShape(10.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            VisionMetric("Brightness", brightness, DroneXColors.PrimaryAccent)
            VisionMetric("Contrast", contrast, DroneXColors.GreenAccent)
            VisionMetric("Sharpness", sharpness, DroneXColors.Warning)
            AnimatedVisibility(visible = error != null) {
                Text(error ?: "", color = DroneXColors.Critical, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun VisionMetric(label: String, value: Double, color: Color) {
    val animated by animateFloatAsState(value.toFloat().coerceIn(0f, 1f), animationSpec = tween(400), label = label)
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(label, color = Color.White.copy(alpha = 0.68f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("${(value * 100).roundToInt()}%", color = color, fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
        LinearProgressIndicator(
            progress = { animated },
            modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = Color.White.copy(alpha = 0.08f)
        )
    }
}

@Composable
private fun VisionQuestionPanel(
    question: String,
    onQuestionChange: (String) -> Unit,
    onSend: () -> Unit,
    history: List<com.example.dn_26.presentation.viewmodel.VisionQuestion>
) {
    Surface(color = DroneXColors.SurfaceDark.copy(alpha = 0.92f), shape = RoundedCornerShape(10.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Ask Vision AI", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = question,
                    onValueChange = onQuestionChange,
                    label = { Text("Question") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
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
                IconButton(onClick = onSend) {
                    Icon(Icons.Default.Send, contentDescription = "Ask", tint = DroneXColors.PrimaryAccent)
                }
            }
            history.take(4).forEach { item ->
                Surface(color = Color.Black.copy(alpha = 0.18f), shape = RoundedCornerShape(8.dp)) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(item.question, color = DroneXColors.PrimaryAccent, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        Text(item.answer, color = Color.White.copy(alpha = 0.78f), fontSize = 10.sp, lineHeight = 14.sp)
                    }
                }
            }
        }
    }
}

private fun com.example.dn_26.domain.model.ConnectionProfile.defaultStreamUrl(): String {
    return when (mode) {
        ConnectionMode.ESP32_WIFI -> "http://$ipAddress:81/stream"
        ConnectionMode.SIMULATION -> "http://$ipAddress:81/stream"
        ConnectionMode.BLUETOOTH -> ""
    }
}

private fun com.example.dn_26.domain.model.ConnectionProfile.defaultSnapshotUrl(): String {
    return when (mode) {
        ConnectionMode.ESP32_WIFI -> "http://$ipAddress:81/capture"
        ConnectionMode.SIMULATION -> "http://$ipAddress:81/capture"
        ConnectionMode.BLUETOOTH -> ""
    }
}

private fun scoreColor(score: Int): Color = when {
    score >= 80 -> DroneXColors.Success
    score >= 55 -> DroneXColors.Warning
    else -> DroneXColors.Critical
}

private fun severityColor(severity: AlertSeverity): Color = when (severity) {
    AlertSeverity.CRITICAL -> DroneXColors.Critical
    AlertSeverity.MEDIUM -> DroneXColors.Warning
    AlertSeverity.LOW -> DroneXColors.Success
}

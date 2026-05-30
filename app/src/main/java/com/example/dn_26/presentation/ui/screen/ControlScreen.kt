package com.example.dn_26.presentation.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dn_26.domain.model.DroneCommand
import com.example.dn_26.presentation.ui.theme.DroneXColors
import com.example.dn_26.presentation.viewmodel.DroneControlViewModel
import com.example.dn_26.presentation.viewmodel.TelemetryViewModel
import com.example.dn_26.presentation.ui.components.VirtualJoystick

@Composable
fun ControlScreen(
    droneControlViewModel: DroneControlViewModel,
    telemetryViewModel: TelemetryViewModel
) {
    val telemetryState by telemetryViewModel.state.collectAsState()
    val droneControlState by droneControlViewModel.state.collectAsState()
    val telemetry = telemetryState.latestTelemetry

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // --- COUCHE 1 : FLUX VIDÉO ---
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "PRIMARY SENSOR: 4K HDR | ZOOM: 1.0x",
                color = Color.White.copy(alpha = 0.05f),
                style = MaterialTheme.typography.displayLarge
            )
        }

        // --- COUCHE 2 : HUD TACTIQUE ---
        Box(modifier = Modifier.fillMaxSize()) {
            ArtificialHorizon(
                pitch = telemetry?.pitch?.toFloat() ?: 0f,
                roll = telemetry?.roll?.toFloat() ?: 0f,
                modifier = Modifier.align(Alignment.Center).size(500.dp)
            )
        }

        // Radar de proximité (Haut Gauche)
        Box(modifier = Modifier.padding(24.dp)) {
            ObstacleRadar(modifier = Modifier.size(150.dp))
        }

        // --- COUCHE 3 : JAUGES TÉLÉMÉTRIQUES ---
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 80.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FlightGauge("ALTITUDE", telemetry?.altitude ?: 0.0, 150.0, "m")
            FlightGauge("VELOCITY", telemetry?.speed ?: 0.0, 50.0, "m/s")
        }

        // --- COUCHE 4 : CONSOLE GAMEPAD ---
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp, start = 48.dp, end = 48.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            VirtualJoystick(
                label = "THRUST / YAW",
                onPositionChange = { x, y -> /* API Command */ },
                size = 180f
            )

            // Centre: Statut Caméra/Gimbal
            PayloadStatusBox()

            VirtualJoystick(
                label = "PITCH / ROLL",
                onPositionChange = { x, y -> /* API Command */ },
                size = 180f
            )
        }

        // --- BOUTONS D'URGENCE ---
        Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            FlightControlButtons(
                viewModel = droneControlViewModel,
                state = droneControlState.droneState.name,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
    }
}

@Composable
fun ObstacleRadar(modifier: Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2

        drawCircle(color = Color.Green.copy(alpha = 0.2f), radius = radius, style = Stroke(2f))
        drawCircle(color = Color.Green.copy(alpha = 0.1f), radius = radius / 2, style = Stroke(1f))
        
        rotate(angle, pivot = center) {
            drawLine(
                color = Color.Green,
                start = center,
                end = Offset(center.x + radius, center.y),
                strokeWidth = 2f
            )
        }
    }
}

@Composable
fun ArtificialHorizon(pitch: Float, roll: Float, modifier: Modifier) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        rotate(degrees = roll, pivot = center) {
            // Ligne d'horizon simplifiée pour le cockpit pro
            drawLine(
                color = Color.White.copy(alpha = 0.5f),
                start = Offset(0f, center.y + pitch),
                end = Offset(size.width, center.y + pitch),
                strokeWidth = 2f
            )
        }
        // Drone mark (Fixed)
        drawLine(color = Color.Yellow, start = Offset(center.x - 80f, center.y), end = Offset(center.x - 20f, center.y), strokeWidth = 4f)
        drawLine(color = Color.Yellow, start = Offset(center.x + 20f, center.y), end = Offset(center.x + 80f, center.y), strokeWidth = 4f)
    }
}

@Composable
fun FlightGauge(label: String, value: Double, max: Double, unit: String) {
    val progress = (value / max).toFloat().coerceIn(0f, 1f)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = Color.Gray)
        Spacer(Modifier.height(8.dp))
        Box(modifier = Modifier.width(16.dp).height(200.dp).background(Color.White.copy(alpha = 0.1f), CircleShape)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(progress)
                    .background(DroneXColors.PrimaryAccent, CircleShape)
                    .align(Alignment.BottomCenter)
            )
        }
        Text("${value.toInt()}$unit", color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PayloadStatusBox() {
    Surface(
        color = Color.Black.copy(alpha = 0.6f),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.padding(bottom = 20.dp).border(1.dp, Color.White.copy(alpha = 0.1f), MaterialTheme.shapes.medium)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Videocam, "REC", tint = Color.Red, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("REC 00:12:45", color = Color.White, fontSize = 12.sp)
        }
    }
}

@Composable
fun FlightControlButtons(viewModel: DroneControlViewModel, state: String, modifier: Modifier) {
    val isFlying = state.contains("FLYING")
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Button(
            onClick = { 
                if (isFlying) viewModel.executeCommand(DroneCommand.LAND)
                else viewModel.executeCommand(DroneCommand.TAKEOFF)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isFlying) DroneXColors.Warning else DroneXColors.PrimaryAccent
            ),
            modifier = Modifier.size(140.dp, 50.dp)
        ) {
            Text(if (isFlying) "LAND" else "TAKEOFF", fontWeight = FontWeight.Black)
        }
        
        IconButton(
            onClick = { viewModel.emergencyStop() },
            modifier = Modifier.size(50.dp).background(DroneXColors.Critical, CircleShape)
        ) {
            Icon(Icons.Default.Dangerous, null, tint = Color.White)
        }
    }
}

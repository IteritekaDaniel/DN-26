package com.example.dn_26.presentation.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dn_26.presentation.ui.theme.DroneXColors
import com.example.dn_26.presentation.ui.components.GlassCard

data class Waypoint(val id: Int, val x: Float, val y: Float, val alt: Int)

@Composable
fun FlightPlanningScreen() {
    var waypoints by remember { mutableStateOf(listOf<Waypoint>()) }
    var selectedWaypoint by remember { mutableStateOf<Waypoint?>(null) }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(DroneXColors.DarkBackground)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- LEFT: MISSION PARAMETERS ---
        Column(
            modifier = Modifier.weight(0.3f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "MISSION PARAMETERS",
                style = MaterialTheme.typography.titleSmall,
                color = Color.Gray,
                letterSpacing = 2.sp
            )

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("MISSION ID: DX-ALPHA-09", color = DroneXColors.PrimaryAccent, fontWeight = FontWeight.Bold)
                    Text("EST. DURATION: 14.5 min", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    Text("TOTAL DISTANCE: 3.4 km", color = Color.White, style = MaterialTheme.typography.bodySmall)
                }
            }

            Text("WAYPOINTS", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            
            Surface(
                color = Color.Black.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                LazyColumn(modifier = Modifier.padding(8.dp)) {
                    items(waypoints) { wp ->
                        WaypointItem(wp, isSelected = selectedWaypoint?.id == wp.id) {
                            selectedWaypoint = wp
                        }
                    }
                }
            }

            Button(
                onClick = { /* Execute Mission */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = DroneXColors.PrimaryAccent)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("UPLOAD MISSION", fontWeight = FontWeight.Bold)
            }
        }

        // --- RIGHT: TACTICAL MAP EDITOR ---
        Box(
            modifier = Modifier
                .weight(0.7f)
                .fillMaxHeight()
                .background(Color.Black.copy(alpha = 0.5f), MaterialTheme.shapes.medium)
                .border(1.dp, Color.White.copy(alpha = 0.1f), MaterialTheme.shapes.medium)
        ) {
            MapGrid(
                waypoints = waypoints,
                onAddWaypoint = { offset ->
                    val newWp = Waypoint(waypoints.size + 1, offset.x, offset.y, 30)
                    waypoints = waypoints + newWp
                }
            )

            Text(
                "TACTICAL MISSION PLANNER V2.0",
                modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
                color = Color.White.copy(alpha = 0.3f),
                style = MaterialTheme.typography.labelSmall
            )
            
            IconButton(
                onClick = { waypoints = emptyList() },
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Clear Map", tint = Color.Gray)
            }
        }
    }
}

@Composable
fun MapGrid(
    waypoints: List<Waypoint>,
    onAddWaypoint: (Offset) -> Unit
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onAddWaypoint(offset)
                }
            }
    ) {
        val width = size.width
        val height = size.height
        
        // Draw grid lines
        val step = 50f
        for (x in 0..(width / step).toInt()) {
            drawLine(
                color = Color.White.copy(alpha = 0.05f),
                start = Offset(x * step, 0f),
                end = Offset(x * step, height),
                strokeWidth = 1f
            )
        }
        for (y in 0..(height / step).toInt()) {
            drawLine(
                color = Color.White.copy(alpha = 0.05f),
                start = Offset(0f, y * step),
                end = Offset(width, y * step),
                strokeWidth = 1f
            )
        }

        // Draw mission path
        if (waypoints.size > 1) {
            val path = Path().apply {
                waypoints.forEachIndexed { index, wp ->
                    if (index == 0) moveTo(wp.x, wp.y) else lineTo(wp.x, wp.y)
                }
            }
            drawPath(
                path = path,
                color = DroneXColors.PrimaryAccent.copy(alpha = 0.6f),
                style = Stroke(width = 3.dp.toPx())
            )
        }

        // Draw waypoints
        waypoints.forEach { wp ->
            drawCircle(
                color = DroneXColors.PrimaryAccent,
                radius = 8.dp.toPx(),
                center = Offset(wp.x, wp.y)
            )
            // Pulse effect simulation would go here
        }
    }
}

@Composable
fun WaypointItem(waypoint: Waypoint, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (isSelected) DroneXColors.PrimaryAccent.copy(alpha = 0.2f) else Color.Transparent,
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "WP ${waypoint.id}",
                color = if (isSelected) DroneXColors.PrimaryAccent else Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.weight(1f))
            Text("${waypoint.alt}m", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
        }
    }
}

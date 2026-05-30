package com.example.dn_26.presentation.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dn_26.presentation.ui.theme.DroneXColors
import kotlin.math.*

/**
 * 🗺️ TACTICAL FLIGHT MAP SCREEN v2.0
 */

@Composable
fun FlightMapScreen(
    onNavigateBack: () -> Unit = {}
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var showGrid by remember { mutableStateOf(true) }
    var showFence by remember { mutableStateOf(true) }
    var showHistory by remember { mutableStateOf(true) }
    var markerCount by remember { mutableIntStateOf(0) }
    
    val flightPath = remember { 
        mutableStateListOf(
            Offset(0f, 0f), Offset(50f, -30f), Offset(100f, 20f), 
            Offset(150f, 80f), Offset(80f, 150f), Offset(-20f, 100f)
        ) 
    }
    
    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale *= zoomChange
        offset += offsetChange
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DroneXColors.BackgroundDark)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .transformable(state = state)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            
            if (showGrid) {
                val gridSize = 100f
                for (i in -10..10) {
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.1f),
                        start = Offset(0f, center.y + i * gridSize),
                        end = Offset(size.width, center.y + i * gridSize),
                        strokeWidth = 1f
                    )
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.1f),
                        start = Offset(center.x + i * gridSize, 0f),
                        end = Offset(center.x + i * gridSize, size.height),
                        strokeWidth = 1f
                    )
                }
            }

            if (showFence) {
                drawCircle(
                    color = DroneXColors.SuccessGreen.copy(alpha = 0.1f),
                    radius = 400f,
                    center = center
                )
                drawCircle(
                    color = DroneXColors.SuccessGreen.copy(alpha = 0.3f),
                    radius = 400f,
                    center = center,
                    style = Stroke(width = 2f)
                )
            }

            if (showHistory && flightPath.size > 1) {
                val path = Path().apply {
                    moveTo(center.x + flightPath[0].x, center.y + flightPath[0].y)
                    for (i in 1 until flightPath.size) {
                        lineTo(center.x + flightPath[i].x, center.y + flightPath[i].y)
                    }
                }
                drawPath(
                    path = path,
                    color = DroneXColors.PrimaryDark,
                    style = Stroke(width = 3f)
                )
            }

            drawCircle(
                color = DroneXColors.InfoBlue,
                radius = 10f,
                center = center
            )

            val currentPos = flightPath.last()
            drawCircle(
                color = DroneXColors.PrimaryDark,
                radius = 12f,
                center = Offset(center.x + currentPos.x, center.y + currentPos.y)
            )

            repeat(markerCount) { index ->
                drawCircle(
                    color = DroneXColors.WarningYellow,
                    radius = 7f,
                    center = Offset(center.x - 140f + index * 36f, center.y - 120f + index * 22f)
                )
            }
        }

        MapOverlayUI(onNavigateBack)
        
        Box(modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp)) {
            CompassView()
        }
        
        MapToolsColumn(
            modifier = Modifier.align(Alignment.CenterEnd).padding(16.dp),
            showGrid = showGrid,
            showFence = showFence,
            showHistory = showHistory,
            onToggleGrid = { showGrid = !showGrid },
            onAddMarker = { markerCount = (markerCount + 1).coerceAtMost(12) },
            onCenter = {
                scale = 1f
                offset = Offset.Zero
                showFence = !showFence
            },
            onToggleHistory = { showHistory = !showHistory }
        )
    }
}

@Composable
fun MapOverlayUI(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.background(DroneXColors.SurfaceDark.copy(alpha = 0.8f), CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
        
        Surface(
            color = DroneXColors.SurfaceDark.copy(alpha = 0.8f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = DroneXColors.SuccessGreen, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("LAT: 48.8566", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("LON: 2.3522", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CompassView() {
    Box(
        modifier = Modifier.size(60.dp).background(DroneXColors.SurfaceDark.copy(alpha = 0.8f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(40.dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            val needlePath = Path().apply {
                moveTo(center.x, center.y - 15f)
                lineTo(center.x - 5f, center.y + 10f)
                lineTo(center.x + 5f, center.y + 10f)
                close()
            }
            drawPath(needlePath, DroneXColors.ErrorRed)
        }
        Text("N", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(bottom = 40.dp))
    }
}

@Composable
fun MapToolsColumn(
    modifier: Modifier = Modifier,
    showGrid: Boolean,
    showFence: Boolean,
    showHistory: Boolean,
    onToggleGrid: () -> Unit,
    onAddMarker: () -> Unit,
    onCenter: () -> Unit,
    onToggleHistory: () -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MapToolButton(Icons.Default.Layers, selected = showGrid, onClick = onToggleGrid)
        MapToolButton(Icons.Default.AddLocationAlt, selected = false, onClick = onAddMarker)
        MapToolButton(Icons.Default.GpsFixed, selected = showFence, onClick = onCenter)
        MapToolButton(Icons.Default.History, selected = showHistory, onClick = onToggleHistory)
    }
}

@Composable
fun MapToolButton(icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.background(
            if (selected) DroneXColors.PrimaryDark.copy(alpha = 0.22f) else DroneXColors.SurfaceDark.copy(alpha = 0.8f),
            RoundedCornerShape(8.dp)
        ).size(44.dp)
    ) {
        Icon(icon, contentDescription = null, tint = DroneXColors.PrimaryDark, modifier = Modifier.size(20.dp))
    }
}

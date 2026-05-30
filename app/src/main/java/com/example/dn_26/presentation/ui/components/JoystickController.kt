package com.example.dn_26.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dn_26.presentation.ui.theme.DroneXColors
import kotlin.math.sqrt

/**
 * Virtual Joystick for drone control
 * Similar to high-end video game controllers
 */
@Composable
fun VirtualJoystick(
    label: String,
    onPositionChange: (x: Float, y: Float) -> Unit,
    modifier: Modifier = Modifier,
    size: Float = 160f
) {
    val density = LocalDensity.current
    val sizePx = with(density) { size.dp.toPx() }
    val maxRadiusPx = with(density) { ((size / 2) - 20f).dp.toPx() }
    
    var joystickPosition by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .size(size.dp)
            .background(
                color = Color(0xFF1A1E37).copy(alpha = 0.8f),
                shape = CircleShape
            )
            .border(
                width = 2.dp,
                color = DroneXColors.PrimaryAccent.copy(alpha = 0.5f),
                shape = CircleShape
            )
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newPosition = joystickPosition + dragAmount
                        val distance = sqrt(newPosition.x * newPosition.x + newPosition.y * newPosition.y)

                        joystickPosition = if (distance <= maxRadiusPx) {
                            newPosition
                        } else {
                            val ratio = maxRadiusPx / distance
                            newPosition * ratio
                        }

                        // Normalize to -1..1 range for the API
                        val normalizedX = joystickPosition.x / maxRadiusPx
                        val normalizedY = -joystickPosition.y / maxRadiusPx
                        onPositionChange(normalizedX, normalizedY)
                    },
                    onDragEnd = {
                        joystickPosition = Offset.Zero
                        onPositionChange(0f, 0f)
                    },
                    onDragCancel = {
                        joystickPosition = Offset.Zero
                        onPositionChange(0f, 0f)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Cross pattern decoration
        Canvas(modifier = Modifier.size(size.dp)) {
            val centerX = sizePx / 2
            val centerY = sizePx / 2
            val lineLength = sizePx * 0.35f
            
            drawLine(
                color = DroneXColors.PrimaryAccent.copy(alpha = 0.2f),
                start = Offset(centerX, centerY - lineLength),
                end = Offset(centerX, centerY + lineLength),
                strokeWidth = 2f
            )
            drawLine(
                color = DroneXColors.PrimaryAccent.copy(alpha = 0.2f),
                start = Offset(centerX - lineLength, centerY),
                end = Offset(centerX + lineLength, centerY),
                strokeWidth = 2f
            )
        }
        
        // Joystick thumb (moving circle)
        Surface(
            modifier = Modifier
                .size(60.dp)
                .offset {
                    IntOffset(
                        joystickPosition.x.toInt(),
                        joystickPosition.y.toInt()
                    )
                },
            color = DroneXColors.PrimaryAccent,
            shape = CircleShape,
            shadowElevation = 8.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    color = Color.White.copy(alpha = 0.2f),
                    shape = CircleShape
                ) {}
            }
        }
        
        // Label
        Text(
            text = label,
            color = DroneXColors.PrimaryAccent,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )
    }
}

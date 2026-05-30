package com.example.dn_26.presentation.ui.gamepad

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dn_26.domain.model.DroneCommand

/**
 * Professional Gamepad UI - Like DualShock 5 / Xbox Series X
 * Perfect for football games style
 */
@Composable
fun ProGamepad(
    onCommand: (command: DroneCommand) -> Unit,
    onMovement: (x: Float, y: Float, z: Float, rotation: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var leftStickX by remember { mutableStateOf(0f) }
    var leftStickY by remember { mutableStateOf(0f) }
    var rightStickX by remember { mutableStateOf(0f) }
    var rightStickY by remember { mutableStateOf(0f) }
    
    var selectedButton by remember { mutableStateOf<String?>(null) }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF0F0F1E),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(24.dp)
            .shadow(elevation = 16.dp, shape = RoundedCornerShape(24.dp))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Title
            Text(
                text = "🎮 DRONE MASTER CONTROLLER",
                color = Color(0xFF00D4FF),
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .background(
                        color = Color(0xFF1A1A2E),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            // Main Gamepad Body
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .background(
                        color = Color(0xFF1A1A2E),
                        shape = RoundedCornerShape(32.dp)
                    )
                    .padding(20.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Gamepad body shadow
                    drawRoundRect(
                        color = Color.Black.copy(alpha = 0.3f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(32f),
                        size = size
                    )
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ==================== LEFT SIDE ====================
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Left Stick Label
                        Text(
                            text = "L STICK",
                            color = Color(0xFF7C3AED),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        // LEFT JOYSTICK
                        ProJoystick(
                            label = "L",
                            color = Color(0xFF7C3AED),
                            size = 120f,
                            onPositionChange = { x, y ->
                                leftStickX = x
                                leftStickY = y
                                onMovement(x, y, rightStickY, rightStickX)
                            }
                        )
                        
                        // L Button
                        Text(
                            text = "L1",
                            color = Color(0xFF7C3AED),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(4.dp)
                                .background(
                                    color = Color(0xFF2D1B69),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    
                    // ==================== CENTER ====================
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(0.6f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Menu Button
                        Text(
                            text = "☰",
                            color = Color(0xFF00D4FF),
                            fontSize = 20.sp,
                            modifier = Modifier
                                .clickable { }
                                .background(
                                    color = Color(0xFF1A1A2E),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(8.dp)
                        )
                        
                        // Action Buttons (Colored buttons)
                        ProButtonGroup(
                            onButtonPress = { button ->
                                selectedButton = button
                                when (button) {
                                    "A" -> onCommand(DroneCommand.TAKEOFF)
                                    "B" -> onCommand(DroneCommand.LAND)
                                    "X" -> onCommand(DroneCommand.CALIBRATE)
                                    "Y" -> onCommand(DroneCommand.RETURN_HOME)
                                }
                            },
                            selectedButton = selectedButton
                        )
                        
                        // Select/Options Button
                        Text(
                            text = "⚙",
                            color = Color(0xFF00D4FF),
                            fontSize = 20.sp,
                            modifier = Modifier
                                .clickable { }
                                .background(
                                    color = Color(0xFF1A1A2E),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(8.dp)
                        )
                    }
                    
                    // ==================== RIGHT SIDE ====================
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Right Stick Label
                        Text(
                            text = "R STICK",
                            color = Color(0xFF10B981),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        // RIGHT JOYSTICK
                        ProJoystick(
                            label = "R",
                            color = Color(0xFF10B981),
                            size = 120f,
                            onPositionChange = { x, y ->
                                rightStickX = x
                                rightStickY = y
                                onMovement(leftStickX, leftStickY, y, x)
                            }
                        )
                        
                        // R Button
                        Text(
                            text = "R1",
                            color = Color(0xFF10B981),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(4.dp)
                                .background(
                                    color = Color(0xFF1B3830),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Status Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFF1A1A2E),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                InfoBadge("L", "${"%.2f".format(leftStickX)}, ${"%.2f".format(leftStickY)}")
                InfoBadge("R", "${"%.2f".format(rightStickX)}, ${"%.2f".format(rightStickY)}")
                InfoBadge("STATUS", "Ready")
            }
        }
    }
}

/**
 * Professional Joystick Design
 */
@Composable
fun ProJoystick(
    label: String,
    color: Color,
    size: Float = 120f,
    onPositionChange: (x: Float, y: Float) -> Unit
) {
    var position by remember { mutableStateOf(Offset.Zero) }
    
    Box(
        modifier = Modifier
            .size(size.dp)
            .background(
                color = Color(0xFF0F0F1E),
                shape = CircleShape
            )
            .border(width = 3.dp, color = color, shape = CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, _ ->
                        val center = Offset(size / 2, size / 2)
                        val delta = change.position - center
                        val distance = kotlin.math.sqrt(delta.x * delta.x + delta.y * delta.y)
                        val maxRadius = (size / 2) - 15
                        
                        position = if (distance <= maxRadius) {
                            delta
                        } else {
                            delta * (maxRadius / distance)
                        }
                        
                        onPositionChange(
                            position.x / maxRadius,
                            position.y / maxRadius
                        )
                    },
                    onDragEnd = {
                        position = Offset.Zero
                        onPositionChange(0f, 0f)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Inner circle
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Background
            drawCircle(
                color = color.copy(alpha = 0.1f),
                radius = size.dp.toPx() / 2
            )
            
            // Grid lines
            val centerX = size.dp.toPx() / 2
            val centerY = size.dp.toPx() / 2
            val radius = (size.dp.toPx() / 2) * 0.7f
            
            drawLine(
                color = color.copy(alpha = 0.2f),
                start = Offset(centerX, centerY - radius),
                end = Offset(centerX, centerY + radius),
                strokeWidth = 1f
            )
            drawLine(
                color = color.copy(alpha = 0.2f),
                start = Offset(centerX - radius, centerY),
                end = Offset(centerX + radius, centerY),
                strokeWidth = 1f
            )
        }
        
        // Thumb
        Box(
            modifier = Modifier
                .size(35.dp)
                .offset(
                    x = (position.x * 0.6).dp,
                    y = (position.y * 0.6).dp
                )
                .background(color = color, shape = CircleShape)
                .shadow(elevation = 8.dp, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = Color(0xFF0F0F1E),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Colored Action Buttons (Like XBOX A, B, X, Y)
 */
@Composable
fun ProButtonGroup(
    onButtonPress: (button: String) -> Unit,
    selectedButton: String? = null
) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(
                color = Color.Transparent
            ),
        contentAlignment = Alignment.Center
    ) {
        // Y Button (Top - Yellow)
        ProActionButton(
            label = "Y",
            color = Color(0xFFFCD34D),
            isPressed = selectedButton == "Y",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-25).dp),
            onClick = { onButtonPress("Y") }
        )
        
        // X Button (Left - Blue)
        ProActionButton(
            label = "X",
            color = Color(0xFF3B82F6),
            isPressed = selectedButton == "X",
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (-25).dp),
            onClick = { onButtonPress("X") }
        )
        
        // B Button (Right - Red)
        ProActionButton(
            label = "B",
            color = Color(0xFFEF4444),
            isPressed = selectedButton == "B",
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = 25.dp),
            onClick = { onButtonPress("B") }
        )
        
        // A Button (Bottom - Green)
        ProActionButton(
            label = "A",
            color = Color(0xFF10B981),
            isPressed = selectedButton == "A",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 25.dp),
            onClick = { onButtonPress("A") }
        )
    }
}

/**
 * Single Action Button
 */
@Composable
fun ProActionButton(
    label: String,
    color: Color,
    isPressed: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(28.dp)
            .background(
                color = if (isPressed) color else color.copy(alpha = 0.7f),
                shape = CircleShape
            )
            .clickable { onClick() }
            .shadow(
                elevation = if (isPressed) 4.dp else 8.dp,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

/**
 * Info Badge
 */
@Composable
fun InfoBadge(
    title: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = title,
            color = Color(0xFF00D4FF),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            color = Color(0xFF10B981),
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

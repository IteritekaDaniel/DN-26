package com.example.dn_26.presentation.ui.gamepad

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dn_26.domain.model.DroneCommand
import java.util.Locale
import kotlin.math.sqrt
import kotlinx.coroutines.delay

/**
 * 🎮 DRONEX PRO - eFootball ULTIMATE DRONE CONTROLLER
 * High-performance virtual controller with stadium-grade HUD and dynamic feedback.
 * Inspired by eFootball / EA Sports FC layout for the DN-26 Project.
 */

enum class FlightDifficulty { ROOKIE, AMATEUR, PRO, ARCADE }

data class GamepadState(
    val leftStickX: Float = 0f,
    val leftStickY: Float = 0f,
    val rightStickX: Float = 0f,
    val rightStickY: Float = 0f,
    val buttonA: Boolean = false,
    val buttonB: Boolean = false,
    val buttonX: Boolean = false,
    val buttonY: Boolean = false,
    val triggerLT: Float = 0f,
    val triggerRT: Float = 0f,
    val bumperLB: Boolean = false,
    val bumperRB: Boolean = false
)

@Composable
fun EliteGamepad(
    modifier: Modifier = Modifier,
    onStateChange: (GamepadState) -> Unit = {},
    onCommand: (DroneCommand) -> Unit = {},
    onMovement: (x: Float, y: Float, z: Float, rotation: Float) -> Unit = { _, _, _, _ -> },
    isDarkMode: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    
    // Match Metrics (eFootball Style)
    var matchScore by remember { mutableIntStateOf(0) }
    var matchSeconds by remember { mutableIntStateOf(0) }
    var difficulty by remember { mutableStateOf(FlightDifficulty.AMATEUR) }
    
    // Physical Input States
    var leftStickPos by remember { mutableStateOf(Offset.Zero) }
    var rightStickPos by remember { mutableStateOf(Offset.Zero) }
    var pressedButtonA by remember { mutableStateOf(false) }
    var pressedButtonB by remember { mutableStateOf(false) }
    var pressedButtonX by remember { mutableStateOf(false) }
    var pressedButtonY by remember { mutableStateOf(false) }
    var pressedLB by remember { mutableStateOf(false) }
    var pressedRB by remember { mutableStateOf(false) }
    var pressedLT by remember { mutableStateOf(false) }
    var pressedRT by remember { mutableStateOf(false) }
    
    val maxRadius = 60f

    // Match Timer Logic
    LaunchedEffect(Unit) {
        while(true) {
            delay(1000)
            matchSeconds++
        }
    }
    
    // Centralized Logic & Feedback
    LaunchedEffect(
        leftStickPos, rightStickPos,
        pressedButtonA, pressedButtonB, pressedButtonX, pressedButtonY,
        pressedLB, pressedRB, pressedLT, pressedRT
    ) {
        val state = GamepadState(
            leftStickX = leftStickPos.x / maxRadius,
            leftStickY = leftStickPos.y / maxRadius,
            rightStickX = rightStickPos.x / maxRadius,
            rightStickY = rightStickPos.y / maxRadius,
            buttonA = pressedButtonA,
            buttonB = pressedButtonB,
            buttonX = pressedButtonX,
            buttonY = pressedButtonY,
            bumperLB = pressedLB,
            bumperRB = pressedRB,
            triggerLT = if (pressedLT) 1f else 0f,
            triggerRT = if (pressedRT) 1f else 0f
        )
        onStateChange(state)
        
        onMovement(
            state.leftStickX,   // Roll
            -state.leftStickY,  // Pitch
            -state.rightStickY, // Throttle
            state.rightStickX   // Yaw
        )
        
        // Command Triggering with Stadium Rewards
        if (pressedButtonA) { 
            onCommand(DroneCommand.TAKEOFF)
            matchScore += 10
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        if (pressedButtonB) { 
            onCommand(DroneCommand.LAND)
            matchScore += 15
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        if (pressedButtonX) { 
            onCommand(DroneCommand.CALIBRATE)
            matchScore += 25
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
        if (pressedButtonY) { 
            onCommand(DroneCommand.RETURN_HOME)
            matchScore += 20
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
    
    val bgColor = if (isDarkMode) Color(0xFF020617) else Color(0xFFF1F5F9)
    val footballBlue = Color(0xFF3B82F6)

    Box(modifier = modifier.padding(8.dp)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(40.dp, RoundedCornerShape(32.dp)),
            color = bgColor,
            shape = RoundedCornerShape(32.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // STADIUM HUD
                StadiumScoreboard(matchScore, matchSeconds)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // DIFFICULTY SELECTOR
                DifficultyTabs(current = difficulty, onSelect = { difficulty = it; haptic.performHapticFeedback(HapticFeedbackType.LongPress) })

                Spacer(modifier = Modifier.height(20.dp))

                // TOP TRIGGERS (LB/RB)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StadiumTrigger("LB", "JOCKEY / CALIB", pressedLB, Color(0xFF94A3B8)) { pressedLB = it }
                    StadiumTrigger("RB", "SPRINT / BOOST", pressedRB, footballBlue) { pressedRB = it }
                }

                // MAIN GAMEPLAY AREA
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // LEFT ANALOG (Player Control)
                    StadiumAnalog(
                        label = "MOVEMENT",
                        pos = leftStickPos,
                        color = footballBlue,
                        onMove = { pos ->
                            val dist = sqrt(pos.x * pos.x + pos.y * pos.y)
                            leftStickPos = if (dist <= maxRadius) pos else (pos / dist) * maxRadius
                        }
                    )

                    // BUTTON DIAMOND
                    FootballDiamond(
                        onA = { pressedButtonA = it },
                        onB = { pressedButtonB = it },
                        onX = { pressedButtonX = it },
                        onY = { pressedButtonY = it },
                        pA = pressedButtonA, pB = pressedButtonB, pX = pressedButtonX, pY = pressedButtonY
                    )

                    // RIGHT ANALOG (Camera / Altitude)
                    StadiumAnalog(
                        label = "ALT / VIEW",
                        pos = rightStickPos,
                        color = Color(0xFFF59E0B),
                        onMove = { pos ->
                            val dist = sqrt(pos.x * pos.x + pos.y * pos.y)
                            rightStickPos = if (dist <= maxRadius) pos else (pos / dist) * maxRadius
                        }
                    )
                }

                // BOTTOM CONTROLS (LT/RT)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StadiumTrigger("LT", "DEFENDER", pressedLT, Color(0xFF10B981)) { pressedLT = it }
                    
                    // Match Status Badge
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "DRONE MATCH: IN FLIGHT",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    StadiumTrigger("RT", "ATTACKER", pressedRT, Color(0xFFEF4444)) { pressedRT = it }
                }
            }
        }
    }
}

@Composable
fun StadiumScoreboard(score: Int, seconds: Int) {
    val mins = seconds / 60
    val secs = seconds % 60
    val timeStr = String.format(Locale.US, "%02d:%02d", mins, secs)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.8f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StadiumStat("SCORE", score.toString().padStart(4, '0'), Color(0xFFFACC15))
        StadiumDivider()
        StadiumStat("TIME", timeStr, Color.White)
        StadiumDivider()
        StadiumStat("BATTERY", "85%", Color(0xFF10B981), icon = Icons.Default.BatteryChargingFull)
    }
}

@Composable
fun StadiumStat(label: String, value: String, color: Color, icon: ImageVector? = null) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(value, color = color, fontSize = 18.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun StadiumDivider() {
    Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color.White.copy(alpha = 0.1f)))
}

@Composable
fun DifficultyTabs(current: FlightDifficulty, onSelect: (FlightDifficulty) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FlightDifficulty.values().forEach { mode ->
            val isSelected = current == mode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) Color(0xFF3B82F6) else Color.White.copy(alpha = 0.05f))
                    .clickable { onSelect(mode) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mode.name,
                    color = if (isSelected) Color.White else Color.Gray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun StadiumAnalog(label: String, pos: Offset, color: Color, onMove: (Offset) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = color, fontSize = 9.sp, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier
                .size(130.dp)
                .background(Brush.radialGradient(listOf(color.copy(alpha = 0.1f), Color.Transparent)), CircleShape)
                .border(2.dp, color.copy(alpha = 0.2f), CircleShape)
                .pointerInput(Unit) {
                    val center = 65.dp.toPx()
                    detectDragGestures(
                        onDrag = { change, _ -> onMove(change.position - Offset(center, center)) },
                        onDragEnd = { onMove(Offset.Zero) }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .graphicsLayer { translationX = pos.x; translationY = pos.y }
                    .size(54.dp)
                    .shadow(16.dp, CircleShape, spotColor = color)
                    .background(Brush.linearGradient(listOf(color, color.darken(0.4f))), CircleShape)
                    .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape)
            )
        }
    }
}

@Composable
fun FootballDiamond(onA: (Boolean) -> Unit, onB: (Boolean) -> Unit, onX: (Boolean) -> Unit, onY: (Boolean) -> Unit, pA: Boolean, pB: Boolean, pX: Boolean, pY: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        DynamicFootballButton("Y", "CANCEL", Color(0xFFFACC15), pY) { onY(it) }
        Row(modifier = Modifier.padding(vertical = 8.dp)) {
            DynamicFootballButton("X", "SKILL", Color(0xFF3B82F6), pX) { onX(it) }
            Spacer(modifier = Modifier.width(16.dp))
            DynamicFootballButton("B", "SHOOT", Color(0xFFEF4444), pB) { onB(it) }
        }
        DynamicFootballButton("A", "PASS", Color(0xFF10B981), pA) { onA(it) }
    }
}

@Composable
fun DynamicFootballButton(label: String, action: String, color: Color, isPressed: Boolean, onState: (Boolean) -> Unit) {
    val scale by animateFloatAsState(if (isPressed) 0.82f else 1f, label = "btnScale")
    val glow by animateFloatAsState(if (isPressed) 0.6f else 0f, label = "btnGlow")

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .size(58.dp)
                .drawBehind {
                    if (isPressed) drawCircle(color, radius = size.maxDimension * 0.75f, alpha = glow)
                }
                .shadow(if (isPressed) 4.dp else 12.dp, CircleShape)
                .clip(CircleShape)
                .background(Brush.verticalGradient(listOf(color, color.darken(0.4f))))
                .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                .pointerInput(Unit) {
                    detectTapGestures(onPress = { onState(true) ; tryAwaitRelease() ; onState(false) })
                },
            contentAlignment = Alignment.Center
        ) {
            Text(label, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
        }
        Text(action, color = Color.White.copy(alpha = 0.5f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StadiumTrigger(label: String, action: String, isPressed: Boolean, color: Color, onState: (Boolean) -> Unit) {
    val activeColor by animateColorAsState(if (isPressed) color else Color.White.copy(alpha = 0.05f), label = "triggerColor")
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, label = "triggerScale")
    
    Box(
        modifier = Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .width(100.dp)
            .height(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(activeColor)
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .pointerInput(Unit) {
                detectTapGestures(onPress = { onState(true) ; tryAwaitRelease() ; onState(false) })
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
            Text(action, color = Color.White.copy(alpha = 0.6f), fontSize = 7.sp, fontWeight = FontWeight.Bold)
        }
    }
}

private fun Color.darken(factor: Float = 0.2f): Color = Color(
    red = red * (1 - factor),
    green = green * (1 - factor),
    blue = blue * (1 - factor),
    alpha = alpha
)

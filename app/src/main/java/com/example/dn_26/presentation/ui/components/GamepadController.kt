package com.example.dn_26.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dn_26.domain.model.DroneCommand

/**
 * Complete virtual gamepad similar to DualShock/Xbox controller
 */
@Composable
fun GamepadController(
    onCommand: (command: DroneCommand, intensity: Float) -> Unit,
    onMovement: (x: Float, y: Float, z: Float, rotation: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var leftJoystickX by remember { mutableStateOf(0f) }
    var leftJoystickY by remember { mutableStateOf(0f) }
    var rightJoystickX by remember { mutableStateOf(0f) }
    var rightJoystickY by remember { mutableStateOf(0f) }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF0A0E27),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        // Title
        Text(
            text = "🎮 DRONE CONTROLLER",
            color = Color(0xFF00D4FF),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Main controller area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT JOYSTICK (Movement)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "MOVEMENT",
                    color = Color(0xFF7C3AED),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                VirtualJoystick(
                    label = "L",
                    onPositionChange = { x, y ->
                        leftJoystickX = x
                        leftJoystickY = y
                        onMovement(x, y, rightJoystickY, rightJoystickX)
                    },
                    size = 140f
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "X: ${"%.2f".format(leftJoystickX)}\nY: ${"%.2f".format(leftJoystickY)}",
                    color = Color(0xFF00D4FF),
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            
            // RIGHT JOYSTICK (Altitude & Rotation)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "ALT & ROTATION",
                    color = Color(0xFF10B981),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                VirtualJoystick(
                    label = "R",
                    onPositionChange = { x, y ->
                        rightJoystickX = x
                        rightJoystickY = y
                        onMovement(leftJoystickX, leftJoystickY, y, x)
                    },
                    size = 140f
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ALT: ${"%.2f".format(rightJoystickY)}\nROT: ${"%.2f".format(rightJoystickX)}",
                    color = Color(0xFF00D4FF),
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Button controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Takeoff button (green)
            ActionButton(
                label = "TAKEOFF ↑",
                color = Color(0xFF10B981),
                modifier = Modifier.weight(1f),
                onClick = { onCommand(DroneCommand.TAKEOFF, 1f) }
            )
            
            // Land button (blue)
            ActionButton(
                label = "LAND ↓",
                color = Color(0xFF3B82F6),
                modifier = Modifier.weight(1f),
                onClick = { onCommand(DroneCommand.LAND, 1f) }
            )
            
            // Emergency button (red)
            ActionButton(
                label = "EMERGENCY",
                color = Color(0xFFEF4444),
                modifier = Modifier.weight(1f),
                onClick = { onCommand(DroneCommand.EMERGENCY_STOP, 1f) }
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Secondary buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionButton(
                label = "CALIBRATE",
                color = Color(0xFFF59E0B),
                modifier = Modifier.weight(1f),
                onClick = { onCommand(DroneCommand.CALIBRATE, 1f) }
            )
            
            ActionButton(
                label = "HOME",
                color = Color(0xFFEC4899),
                modifier = Modifier.weight(1f),
                onClick = { onCommand(DroneCommand.RETURN_HOME, 1f) }
            )
        }
    }
}

/**
 * Action button for gamepad
 */
@Composable
fun ActionButton(
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxHeight(),
        colors = ButtonDefaults.buttonColors(
            containerColor = color
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

package com.example.dn_26.presentation.ui.gamepad

import android.view.KeyEvent
import android.view.MotionEvent
import com.example.dn_26.domain.model.DroneCommand
import kotlin.math.abs

/**
 * Handles physical gamepad/controller input
 * Supports standard Android game controllers (Xbox, PlayStation, etc.)
 */
class PhysicalGamepadHandler {
    var onMovement: (x: Float, y: Float, z: Float, rotation: Float) -> Unit = { _, _, _, _ -> }
    var onCommand: (command: DroneCommand, intensity: Float) -> Unit = { _, _ -> }
    
    // Analog stick sensitivity (0-1)
    private val stickDeadzone = 0.2f
    
    fun onGenericMotionEvent(event: MotionEvent): Boolean {
        // Get left joystick (movement)
        val x = getCenteredAxis(event, MotionEvent.AXIS_X)
        val y = getCenteredAxis(event, MotionEvent.AXIS_Y)
        
        // Get right joystick (altitude & rotation)
        val z = getCenteredAxis(event, MotionEvent.AXIS_Z)
        val rotation = getCenteredAxis(event, MotionEvent.AXIS_RZ)
        
        // Get triggers
        val l2 = getCenteredAxis(event, MotionEvent.AXIS_LTRIGGER)
        val r2 = getCenteredAxis(event, MotionEvent.AXIS_RTRIGGER)
        
        // Apply deadzone
        val cleanX = if (abs(x) > stickDeadzone) x else 0f
        val cleanY = if (abs(y) > stickDeadzone) y else 0f
        val cleanZ = if (abs(z) > stickDeadzone) z else 0f
        val cleanRotation = if (abs(rotation) > stickDeadzone) rotation else 0f
        
        // Triggers for commands
        if (abs(l2) > 0.5f) {
            onCommand(DroneCommand.TAKEOFF, l2)
        }
        if (abs(r2) > 0.5f) {
            onCommand(DroneCommand.LAND, r2)
        }
        
        // Movement
        onMovement(cleanX, cleanY, cleanZ, cleanRotation)
        
        return true
    }
    
    fun onKeyDown(keyCode: Int): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_BUTTON_A -> {
                onCommand(DroneCommand.TAKEOFF, 1f)
                true
            }
            KeyEvent.KEYCODE_BUTTON_B -> {
                onCommand(DroneCommand.LAND, 1f)
                true
            }
            KeyEvent.KEYCODE_BUTTON_X -> {
                onCommand(DroneCommand.CALIBRATE, 1f)
                true
            }
            KeyEvent.KEYCODE_BUTTON_Y -> {
                onCommand(DroneCommand.RETURN_HOME, 1f)
                true
            }
            KeyEvent.KEYCODE_BUTTON_START -> {
                onCommand(DroneCommand.EMERGENCY_STOP, 1f)
                true
            }
            else -> false
        }
    }
    
    private fun getCenteredAxis(
        event: MotionEvent,
        axis: Int
    ): Float {
        val range = event.device.getMotionRange(axis, event.source)
        range?.let {
            val flat = it.flat
            val value = event.getAxisValue(axis)
            
            return if (abs(value) > flat) {
                value
            } else {
                0f
            }
        }
        return 0f
    }
}

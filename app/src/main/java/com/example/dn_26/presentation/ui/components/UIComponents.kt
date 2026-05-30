package com.example.dn_26.presentation.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dn_26.presentation.ui.theme.DroneXColors

/**
 * Radar Tactique de détection d'obstacles
 */
@Composable
fun TacticalRadar(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "sweep"
    )

    Box(modifier = modifier.size(140.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2

            // Anneaux de distance
            for (i in 1..3) {
                drawCircle(
                    color = DroneXColors.Success.copy(alpha = 0.1f * i),
                    radius = radius * (i / 3f),
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // Balayage
            rotate(angle, pivot = center) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0f to Color.Transparent,
                        0.5f to DroneXColors.Success.copy(alpha = 0.4f),
                        1f to DroneXColors.Success
                    ),
                    startAngle = 0f,
                    sweepAngle = 90f,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = size
                )
            }
            
            // Simuler des cibles
            drawCircle(color = Color.Red, radius = 4f, center = Offset(center.x + 40f, center.y - 30f))
            drawCircle(color = DroneXColors.PrimaryAccent, radius = 4f, center = Offset(center.x - 20f, center.y + 50f))
        }
        Text("RADAR SCAN", color = Color.White.copy(alpha = 0.3f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
    }
}

/**
 * Boussole Aéronautique Pro
 */
@Composable
fun AviationCompass(heading: Float, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(100.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2

            rotate(-heading, pivot = center) {
                // Graduation
                for (i in 0 until 360 step 30) {
                    val angleRad = Math.toRadians(i.toDouble() - 90).toFloat()
                    val start = Offset(
                        center.x + (radius - 10.dp.toPx()) * kotlin.math.cos(angleRad),
                        center.y + (radius - 10.dp.toPx()) * kotlin.math.sin(angleRad)
                    )
                    val end = Offset(
                        center.x + radius * kotlin.math.cos(angleRad),
                        center.y + radius * kotlin.math.sin(angleRad)
                    )
                    drawLine(color = Color.White.copy(alpha = 0.5f), start = start, end = end, strokeWidth = 2f)
                }
            }

            // Aiguille fixe (Nord haut)
            val needlePath = Path().apply {
                moveTo(center.x, center.y - radius + 5.dp.toPx())
                lineTo(center.x - 6.dp.toPx(), center.y - radius + 20.dp.toPx())
                lineTo(center.x + 6.dp.toPx(), center.y - radius + 20.dp.toPx())
                close()
            }
            drawPath(path = needlePath, color = Color.Red)
        }
        Text("${heading.toInt()}°", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
    }
}

/**
 * Speed Gauge for Dashboard
 */
@Composable
fun SpeedGauge(speed: Double, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "speed")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glow"
    )

    Box(contentAlignment = Alignment.Center, modifier = modifier.size(180.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2 - 10.dp.toPx()

            // Arc background
            drawArc(
                color = Color.White.copy(alpha = 0.1f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = 12.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )

            // Speed arc
            val sweepAngle = (speed.toFloat() / 50f * 270f).coerceAtMost(270f)
            drawArc(
                brush = Brush.sweepGradient(
                    0f to DroneXColors.PrimaryAccent.copy(alpha = 0.5f),
                    1f to DroneXColors.PrimaryAccent
                ),
                startAngle = 135f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = 12.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
            
            // Glow effect
            drawArc(
                color = DroneXColors.PrimaryAccent.copy(alpha = glowAlpha * 0.2f),
                startAngle = 135f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = 20.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${speed.toInt()}",
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "KM/H",
                color = DroneXColors.PrimaryAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
    }
}

/**
 * Glassmorphism Card (Ré-implémentée pour consistance)
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        color = Color(0xFF1A1F3A).copy(alpha = 0.6f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        modifier = modifier
    ) {
        Box(modifier = Modifier.padding(12.dp)) {
            content()
        }
    }
}

/**
 * Bouton Animé (Ré-implémenté pour consistance)
 */
@Composable
fun AnimatedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = DroneXColors.PrimaryAccent
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.height(48.dp)
    ) {
        Text(text.uppercase(), fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
    }
}

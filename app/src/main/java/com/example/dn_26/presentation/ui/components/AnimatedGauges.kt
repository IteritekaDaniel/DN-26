package com.example.dn_26.presentation.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dn_26.presentation.ui.theme.DroneXColors

/**
 * 📈 PROFESSIONAL ANIMATED GAUGES v2.0
 * 
 * High-performance custom-drawn gauges for critical telemetry.
 */

@Composable
fun CircularTelemetryGauge(
    value: Float,
    maxValue: Float,
    label: String,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val animatedValue = remember { Animatable(0f) }
    
    LaunchedEffect(value) {
        animatedValue.animateTo(
            targetValue = value / maxValue,
            animationSpec = tween(durationMillis = 1000)
        )
    }

    Box(
        modifier = modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 12.dp.toPx()
            
            // Background Track
            drawArc(
                color = color.copy(alpha = 0.1f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            
            // Active Progress
            drawArc(
                brush = Brush.sweepGradient(
                    0f to color.copy(alpha = 0.5f),
                    1f to color
                ),
                startAngle = 135f,
                sweepAngle = 270f * animatedValue.value,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${value.toInt()}",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = unit.uppercase(),
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                color = Color.Gray,
                fontSize = 9.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun HorizontalSignalGauge(
    level: Int, // 0-100
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 11.sp,
            modifier = Modifier.width(80.dp)
        )
        
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(5) { index ->
                val barActive = level > (index * 20)
                val barColor = when {
                    !barActive -> Color.Gray.copy(alpha = 0.2f)
                    level < 30 -> DroneXColors.ErrorRed
                    level < 70 -> DroneXColors.WarningYellow
                    else -> DroneXColors.SuccessGreen
                }
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp + (index * 4).dp)
                        .background(barColor, RoundedCornerShape(2.dp))
                )
            }
        }
        
        Text(
            text = "$level%",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

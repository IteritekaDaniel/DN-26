package com.example.dn_26.presentation.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dn_26.domain.model.Telemetry
import com.example.dn_26.presentation.ui.theme.DroneXColors
import com.example.dn_26.presentation.ui.components.GlassCard

@Composable
fun DataAnalyticsScreen(samples: List<Telemetry> = emptyList()) {
    val altitude = samples.takeLast(40).map { it.altitude.toFloat() }.ifEmpty { listOf(10f, 25f, 45f, 40f, 60f, 85f, 80f) }
    val speed = samples.takeLast(40).map { (it.speed * 3.6).toFloat() }.ifEmpty { listOf(2f, 5f, 12f, 15f, 10f, 8f, 14f) }
    val battery = samples.takeLast(40).map { it.batteryVoltage.toFloat() }.ifEmpty { listOf(12.6f, 12.5f, 12.4f, 12.2f, 12.3f, 12.1f, 11.9f) }
    val thermal = samples.takeLast(40).map { it.temperature.toFloat() }.ifEmpty { listOf(31f, 35f, 42f, 38f, 44f, 46f, 40f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DroneXColors.DarkBackground)
            .padding(24.dp)
    ) {
        Text(
            text = "TELEMETRY ANALYTICS HUB",
            style = MaterialTheme.typography.headlineMedium,
            color = DroneXColors.PrimaryAccent,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item { AnalyticsChartCard("ALTITUDE TREND (m)", altitude) }
            item { AnalyticsChartCard("SPEED VELOCITY (km/h)", speed) }
            item { AnalyticsChartCard("BATTERY DISCHARGE (V)", battery) }
            item { AnalyticsChartCard("THERMAL LOAD (C)", thermal) }
        }
    }
}

@Composable
fun AnalyticsChartCard(title: String, data: List<Float>) {
    GlassCard(modifier = Modifier.fillMaxWidth().height(220.dp)) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
            SimpleLineChart(data)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("T-15m", style = MaterialTheme.typography.labelSmall, color = Color.DarkGray)
                Text("LIVE", style = MaterialTheme.typography.labelSmall, color = DroneXColors.PrimaryAccent)
            }
        }
    }
}

@Composable
fun SimpleLineChart(data: List<Float>) {
    Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
        val points = if (data.size < 2) data + data else data
        val maxVal = points.maxOrNull() ?: 1f
        val minVal = points.minOrNull() ?: 0f
        val range = (maxVal - minVal).coerceAtLeast(1f)
        
        val width = size.width
        val height = size.height
        val stepX = width / (points.size - 1)

        val path = Path().apply {
            points.forEachIndexed { index, value ->
                val x = index * stepX
                val y = height - ((value - minVal) / range * height)
                if (index == 0) moveTo(x, y) else lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = DroneXColors.PrimaryAccent,
            style = Stroke(width = 3.dp.toPx())
        )
        
        // Draw points
        points.forEachIndexed { index, value ->
            val x = index * stepX
            val y = height - ((value - minVal) / range * height)
            drawCircle(color = Color.White, radius = 4f, center = Offset(x, y))
        }
    }
}

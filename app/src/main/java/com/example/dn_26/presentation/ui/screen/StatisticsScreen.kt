package com.example.dn_26.presentation.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dn_26.presentation.ui.theme.DroneXColors

/**
 * 📊 PROFESSIONAL STATISTICS SCREEN v2.0 - FIXED
 */

@Composable
fun StatisticsScreen(
    onNavigateBack: () -> Unit = {}
) {
    val stats = listOf(
        StatCardData("Total Distance", "124.5 km", Icons.Default.Route, DroneXColors.PrimaryDark),
        StatCardData("Flight Time", "18h 42m", Icons.Default.Timer, DroneXColors.SuccessGreen),
        StatCardData("Max Altitude", "150m", Icons.AutoMirrored.Filled.TrendingUp, DroneXColors.WarningYellow),
        StatCardData("Safety Score", "98/100", Icons.Default.VerifiedUser, DroneXColors.InfoBlue)
    )

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            CenterAlignedTopAppBar(
                title = { Text("FLIGHT ANALYTICS", fontWeight = FontWeight.Black, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DroneXColors.BackgroundDark,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = DroneXColors.BackgroundDark
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats Grid
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(stats[0], Modifier.weight(1f))
                    StatCard(stats[1], Modifier.weight(1f))
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(stats[2], Modifier.weight(1f))
                    StatCard(stats[3], Modifier.weight(1f))
                }
            }

            // Altitude Chart Section
            item {
                SectionHeader("Altitude Trend (Last 7 Flights)")
                PerformanceChart(
                    dataPoints = listOf(20f, 45f, 30f, 80f, 60f, 90f, 75f),
                    color = DroneXColors.PrimaryDark
                )
            }

            // Recent Logs Section
            item { SectionHeader("Recent Flight Logs") }
            items(getMockFlightLogs()) { log ->
                FlightLogItem(log)
            }
            
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

data class StatCardData(val label: String, val value: String, val icon: ImageVector, val color: Color)

@Composable
fun StatCard(data: StatCardData, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.shadow(8.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = DroneXColors.SurfaceDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(data.icon, contentDescription = null, tint = data.color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(data.value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
            Text(data.label, color = Color.Gray, fontSize = 10.sp)
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        color = DroneXColors.PurpleAccent,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun PerformanceChart(dataPoints: List<Float>, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth().height(180.dp).shadow(8.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = DroneXColors.SurfaceDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            val width = size.width
            val height = size.height
            val maxData = dataPoints.maxOrNull() ?: 1f
            val spacePerPoint = width / (dataPoints.size - 1)

            val path = Path().apply {
                dataPoints.forEachIndexed { index, value ->
                    val x = index * spacePerPoint
                    val y = height - (value / maxData * height)
                    if (index == 0) moveTo(x, y) else lineTo(x, y)
                }
            }

            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 4f)
            )
            
            // Draw points
            dataPoints.forEachIndexed { index, value ->
                val x = index * spacePerPoint
                val y = height - (value / maxData * height)
                drawCircle(color = color, radius = 6f, center = Offset(x, y))
                drawCircle(color = Color.White, radius = 2f, center = Offset(x, y))
            }
        }
    }
}

data class LogData(val id: String, val date: String, val duration: String, val distance: String, val status: String)

@Composable
fun FlightLogItem(log: LogData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DroneXColors.SurfaceDark, RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).background(DroneXColors.BackgroundDark, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.FlightTakeoff, contentDescription = null, tint = DroneXColors.PrimaryDark, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Flight #${log.id}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(log.date, color = Color.Gray, fontSize = 12.sp)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(log.distance, color = DroneXColors.SuccessGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(log.duration, color = Color.Gray, fontSize = 12.sp)
        }
    }
}

fun getMockFlightLogs() = listOf(
    LogData("1024", "Oct 12, 2024", "24m 12s", "4.2 km", "Success"),
    LogData("1023", "Oct 10, 2024", "18m 45s", "2.8 km", "Success"),
    LogData("1022", "Oct 08, 2024", "42m 05s", "8.5 km", "Success"),
    LogData("1021", "Oct 05, 2024", "12m 30s", "1.4 km", "Aborted")
)

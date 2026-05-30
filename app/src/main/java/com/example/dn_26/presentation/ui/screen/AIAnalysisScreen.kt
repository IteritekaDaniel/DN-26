package com.example.dn_26.presentation.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dn_26.domain.model.Anomaly
import com.example.dn_26.domain.model.Prediction
import com.example.dn_26.domain.model.Telemetry
import com.example.dn_26.presentation.viewmodel.AIViewModel
import com.example.dn_26.presentation.ui.theme.DroneXColors
import com.example.dn_26.presentation.ui.components.*

@Composable
fun AIAnalysisScreen(
    aiViewModel: AIViewModel,
    latestTelemetry: Telemetry? = null,
    recentTelemetry: List<Telemetry> = emptyList(),
    modifier: Modifier = Modifier
) {
    val aiState by aiViewModel.state.collectAsState()
    var isDiagnosing by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DroneXColors.DarkBackground)
            .padding(24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AI COGNITIVE ENGINE",
                    style = MaterialTheme.typography.headlineMedium,
                    color = DroneXColors.PrimaryAccent,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Neural analysis of real-time telemetry streams",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            Button(
                onClick = {
                    isDiagnosing = true
                    latestTelemetry?.let { aiViewModel.analyzeTelemetry(it, recentTelemetry) }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DroneXColors.SecondaryAccent),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.AutoFixHigh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("RUN FULL DIAGNOSTIC")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        if (isDiagnosing) {
            DiagnosticProgress { isDiagnosing = false }
        }

        AnalysisSummary(aiState)

        Row(
            modifier = Modifier.fillMaxSize(), 
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Panneau des Anomalies
            Column(modifier = Modifier.weight(1f)) {
                SectionHeader("DETECTED ANOMALIES", Icons.Default.NearbyError)
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(aiState.anomalies) { anomaly ->
                        AnomalyItem(anomaly)
                    }
                    if (aiState.anomalies.isEmpty()) {
                        item { EmptyState("System integrity nominal. No anomalies.") }
                    }
                }
            }

            // Panneau des Prédictions
            Column(modifier = Modifier.weight(1f)) {
                SectionHeader("PREDICTIVE MAINTENANCE", Icons.Default.ModelTraining)
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(aiState.predictions) { prediction ->
                        PredictionItem(prediction)
                    }
                    if (aiState.predictions.isEmpty()) {
                        item { EmptyState("Insufficient data for predictive modeling.") }
                    }
                }
            }
        }
    }
}

@Composable
fun AnalysisSummary(aiState: com.example.dn_26.presentation.viewmodel.AIState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(color = Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("RISK SCORE", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("${aiState.riskScore.toInt()}/100", color = if (aiState.riskScore > 65) DroneXColors.Critical else DroneXColors.PrimaryAccent, fontSize = 24.sp, fontWeight = FontWeight.Black)
            }
        }
        Surface(color = Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(2f)) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("ACTIONABLE RECOMMENDATION", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(aiState.recommendations.firstOrNull() ?: "Run diagnostic after telemetry starts.", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DiagnosticProgress(onComplete: () -> Unit) {
    var progress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "")

    LaunchedEffect(Unit) {
        progress = 1f
        kotlinx.coroutines.delay(2000)
        onComplete()
    }

    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = DroneXColors.PrimaryAccent,
            trackColor = Color.White.copy(alpha = 0.1f)
        )
    }
}

@Composable
fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
        Icon(icon, contentDescription = null, tint = DroneXColors.PrimaryAccent, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, style = MaterialTheme.typography.labelLarge, color = Color.White, letterSpacing = 2.sp)
    }
}

@Composable
fun EmptyState(text: String) {
    Box(
        modifier = Modifier.fillMaxWidth().height(80.dp).border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.DarkGray, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun AnomalyItem(anomaly: Anomaly) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(anomaly.category, color = DroneXColors.Warning, fontWeight = FontWeight.Bold)
                Text("${(anomaly.confidence * 100).toInt()}% CONF.", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Text(anomaly.description, color = Color.White, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 4.dp))
            LinearProgressIndicator(
                progress = { anomaly.confidence.toFloat() },
                modifier = Modifier.fillMaxWidth().height(2.dp),
                color = DroneXColors.Warning
            )
        }
    }
}

@Composable
fun PredictionItem(prediction: Prediction) {
    Surface(
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(prediction.component, color = DroneXColors.PrimaryAccent, fontWeight = FontWeight.Bold)
                Text("Failure risk: ${prediction.failureType}", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
            }
            CircularProgressIndicator(
                progress = { prediction.probability.toFloat() },
                modifier = Modifier.size(32.dp),
                color = if (prediction.probability > 0.7) DroneXColors.Critical else DroneXColors.Success,
                strokeWidth = 3.dp
            )
        }
    }
}

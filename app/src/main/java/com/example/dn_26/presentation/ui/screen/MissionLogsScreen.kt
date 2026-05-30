package com.example.dn_26.presentation.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dn_26.domain.model.Alert
import com.example.dn_26.domain.model.AlertSeverity
import com.example.dn_26.presentation.viewmodel.AlertViewModel
import com.example.dn_26.presentation.ui.theme.DroneXColors
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MissionLogsScreen(
    alertViewModel: AlertViewModel,
    modifier: Modifier = Modifier
) {
    val alertState by alertViewModel.state.collectAsState()
    val dateFormatter = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DroneXColors.DarkBackground)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, tint = DroneXColors.PrimaryAccent, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "SYSTEM AUDIT LOGS",
                style = MaterialTheme.typography.headlineMedium,
                color = DroneXColors.PrimaryAccent,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            color = Color.Black.copy(alpha = 0.3f),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxSize()
        ) {
            if (alertState.alerts.isEmpty()) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text("No mission events recorded", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(alertState.alerts) { alert ->
                        LogEntry(alert, dateFormatter)
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                    }
                }
            }
        }
    }
}

@Composable
fun LogEntry(alert: Alert, formatter: SimpleDateFormat) {
    val statusColor = when (alert.severity) {
        AlertSeverity.CRITICAL -> DroneXColors.Critical
        AlertSeverity.MEDIUM -> DroneXColors.Warning
        AlertSeverity.LOW -> DroneXColors.Info
    }

    val icon = when (alert.severity) {
        AlertSeverity.CRITICAL -> Icons.Default.Error
        AlertSeverity.MEDIUM -> Icons.Default.Warning
        else -> Icons.Default.Info
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formatter.format(Date(alert.timestamp)),
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            modifier = Modifier.width(90.dp)
        )
        
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = statusColor,
            modifier = Modifier.size(16.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "[${alert.type}] ${alert.title}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
            Text(
                text = alert.message,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

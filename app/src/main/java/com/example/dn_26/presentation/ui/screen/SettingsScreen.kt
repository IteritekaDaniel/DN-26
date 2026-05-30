package com.example.dn_26.presentation.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Complete Settings Screen with all drone configurations
 */
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {}
) {
    var selectedCategory by remember { mutableStateOf("connection") }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E27))
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        SettingsHeader(onNavigateBack)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Category Tabs
        SettingsCategoryTabs(
            selectedCategory = selectedCategory,
            onCategorySelected = { selectedCategory = it }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Content based on category
        when (selectedCategory) {
            "connection" -> ConnectionSettings()
            "thresholds" -> ThresholdSettings()
            "calibration" -> CalibrationSettings()
            "advanced" -> AdvancedSettings()
            "about" -> AboutSettings()
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * Settings Header
 */
@Composable
fun SettingsHeader(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1E37))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Back",
            tint = Color(0xFF00D4FF),
            modifier = Modifier
                .size(28.dp)
                .clickable { onNavigateBack() }
        )
        
        Text(
            text = "⚙️ SETTINGS",
            color = Color(0xFF00D4FF),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Settings",
            tint = Color(0xFF00D4FF),
            modifier = Modifier.size(28.dp)
        )
    }
}

/**
 * Settings Category Tabs
 */
@Composable
fun SettingsCategoryTabs(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf(
        "connection" to "📡 Connection",
        "thresholds" to "⚠️ Thresholds",
        "calibration" to "🔧 Calibration",
        "advanced" to "🛠️ Advanced",
        "about" to "ℹ️ About"
    )
    
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories.size) { index ->
            val (key, label) = categories[index]
            SettingsCategoryTab(
                label = label,
                isSelected = selectedCategory == key,
                onClick = { onCategorySelected(key) }
            )
        }
    }
}

@Composable
fun SettingsCategoryTab(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF00D4FF) else Color(0xFF1A1E37)
        ),
        modifier = Modifier
            .height(40.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) Color(0xFF0A0E27) else Color(0xFF00D4FF),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ==================== CONNECTION SETTINGS ====================

@Composable
fun ConnectionSettings() {
    var ipAddress by remember { mutableStateOf("192.168.4.1") }
    var port by remember { mutableStateOf("8080") }
    var connectionType by remember { mutableStateOf("WiFi") }
    var isConnected by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Connection Settings",
            color = Color(0xFF00D4FF),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Connection Type
        SettingItemToggle(
            icon = Icons.Default.SignalCellularAlt,
            label = "Connection Type",
            value = connectionType,
            options = listOf("WiFi", "Bluetooth", "USB Serial"),
            onValueChange = { connectionType = it }
        )
        
        // IP Address
        SettingItemTextField(
            icon = Icons.Default.Router,
            label = "IP Address",
            value = ipAddress,
            onValueChange = { ipAddress = it }
        )
        
        // Port
        SettingItemTextField(
            icon = Icons.Default.Numbers,
            label = "Port",
            value = port,
            onValueChange = { port = it }
        )
        
        // Connection Status
        SettingItemStatus(
            icon = Icons.Default.Cloud,
            label = "Status",
            status = if (isConnected) "Connected" else "Disconnected",
            statusColor = if (isConnected) Color(0xFF10B981) else Color(0xFFEF4444)
        )
        
        // Connect Button
        Button(
            onClick = { isConnected = !isConnected },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isConnected) Color(0xFFEF4444) else Color(0xFF10B981)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = if (isConnected) "DISCONNECT" else "CONNECT",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

// ==================== THRESHOLD SETTINGS ====================

@Composable
fun ThresholdSettings() {
    var batteryWarning by remember { mutableStateOf(25) }
    var batteryLow by remember { mutableStateOf(10) }
    var tempMax by remember { mutableStateOf(65) }
    var gpsMinSatellites by remember { mutableStateOf(8) }
    var windMaxSpeed by remember { mutableStateOf(12) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Alert Thresholds",
            color = Color(0xFF00D4FF),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Battery Warning
        SettingItemSlider(
            icon = Icons.Default.BatteryAlert,
            label = "Battery Warning Level",
            value = batteryWarning,
            range = 10..100,
            unit = "%",
            onValueChange = { batteryWarning = it }
        )
        
        // Battery Critical
        SettingItemSlider(
            icon = Icons.Default.BatteryChargingFull,
            label = "Battery Critical Level",
            value = batteryLow,
            range = 0..50,
            unit = "%",
            onValueChange = { batteryLow = it }
        )
        
        // Temperature Max
        SettingItemSlider(
            icon = Icons.Default.Thermostat,
            label = "Max Temperature",
            value = tempMax,
            range = 40..100,
            unit = "°C",
            onValueChange = { tempMax = it }
        )
        
        // GPS Min Satellites
        SettingItemSlider(
            icon = Icons.Default.Satellite,
            label = "Min GPS Satellites",
            value = gpsMinSatellites,
            range = 4..20,
            unit = "",
            onValueChange = { gpsMinSatellites = it }
        )
        
        // Wind Max Speed
        SettingItemSlider(
            icon = Icons.Default.Air,
            label = "Max Wind Speed",
            value = windMaxSpeed,
            range = 5..30,
            unit = "m/s",
            onValueChange = { windMaxSpeed = it }
        )
    }
}

// ==================== CALIBRATION SETTINGS ====================

@Composable
fun CalibrationSettings() {
    var isCalibrating by remember { mutableStateOf(false) }
    var calibrationProgress by remember { mutableStateOf(0f) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Calibration",
            color = Color(0xFF00D4FF),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // IMU Calibration
        SettingItemButton(
            icon = Icons.Default.Vibration,
            label = "IMU Calibration",
            description = "Calibrate accelerometer & gyroscope",
            onClick = { isCalibrating = true }
        )
        
        // Compass Calibration
        SettingItemButton(
            icon = Icons.Default.Explore,
            label = "Compass Calibration",
            description = "Calibrate magnetometer",
            onClick = { isCalibrating = true }
        )
        
        // ESC Calibration
        SettingItemButton(
            icon = Icons.Default.Adjust,
            label = "ESC Calibration",
            description = "Calibrate motor controllers",
            onClick = { isCalibrating = true }
        )
        
        // Battery Calibration
        SettingItemButton(
            icon = Icons.Default.BatteryChargingFull,
            label = "Battery Calibration",
            description = "Calibrate battery meter",
            onClick = { isCalibrating = true }
        )
        
        // Calibration Progress
        if (isCalibrating) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Calibrating...",
                color = Color(0xFF10B981),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            
            LinearProgressIndicator(
                progress = { calibrationProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color(0xFF10B981),
                trackColor = Color(0xFF1A1E37)
            )
            
            LaunchedEffect(isCalibrating) {
                for (i in 0..100) {
                    delay(50)
                    calibrationProgress = i / 100f
                }
                isCalibrating = false
                calibrationProgress = 0f
            }
        }
    }
}

// ==================== ADVANCED SETTINGS ====================

@Composable
fun AdvancedSettings() {
    var telemetryFrequency by remember { mutableStateOf(10) }
    var pidP by remember { mutableStateOf(1.5f) }
    var pidI by remember { mutableStateOf(0.1f) }
    var pidD by remember { mutableStateOf(0.8f) }
    var enableLogging by remember { mutableStateOf(true) }
    var enableDebugMode by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Advanced Settings",
            color = Color(0xFF00D4FF),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Telemetry Frequency
        SettingItemSlider(
            icon = Icons.Default.Speed,
            label = "Telemetry Frequency",
            value = telemetryFrequency,
            range = 1..100,
            unit = "Hz",
            onValueChange = { telemetryFrequency = it }
        )
        
        HorizontalDivider(color = Color(0xFF1A1E37), thickness = 1.dp)
        
        Text(
            text = "PID Controller Tuning",
            color = Color(0xFF7C3AED),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        // PID P Value
        SettingItemSliderFloat(
            icon = Icons.Default.Tune,
            label = "P (Proportional)",
            value = pidP,
            range = 0f..3f,
            onValueChange = { pidP = it }
        )
        
        // PID I Value
        SettingItemSliderFloat(
            icon = Icons.Default.Tune,
            label = "I (Integral)",
            value = pidI,
            range = 0f..1f,
            onValueChange = { pidI = it }
        )
        
        // PID D Value
        SettingItemSliderFloat(
            icon = Icons.Default.Tune,
            label = "D (Derivative)",
            value = pidD,
            range = 0f..2f,
            onValueChange = { pidD = it }
        )
        
        HorizontalDivider(color = Color(0xFF1A1E37), thickness = 1.dp)
        
        // Debug Options
        SettingItemToggleSwitch(
            icon = Icons.Default.Info,
            label = "Enable Logging",
            isChecked = enableLogging,
            onCheckedChange = { enableLogging = it }
        )
        
        SettingItemToggleSwitch(
            icon = Icons.Default.BugReport,
            label = "Debug Mode",
            isChecked = enableDebugMode,
            onCheckedChange = { enableDebugMode = it }
        )
    }
}

// ==================== ABOUT SETTINGS ====================

@Composable
fun AboutSettings() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "About",
            color = Color(0xFF00D4FF),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // App Info
        SettingItemInfo(
            label = "App Name",
            value = "DroneX Pro"
        )
        
        SettingItemInfo(
            label = "Version",
            value = "1.0.0"
        )
        
        SettingItemInfo(
            label = "Build",
            value = "2024.01.001"
        )
        
        SettingItemInfo(
            label = "Firmware Version",
            value = "ESP32-v1.2.3"
        )
        
        HorizontalDivider(color = Color(0xFF1A1E37), thickness = 1.dp)
        
        // Links
        SettingItemLink(
            label = "Website",
            value = "https://dronex.pro"
        )
        
        SettingItemLink(
            label = "Documentation",
            value = "https://docs.dronex.pro"
        )
        
        SettingItemLink(
            label = "Support",
            value = "support@dronex.pro"
        )
    }
}

// ==================== REUSABLE COMPONENTS ====================

@Composable
fun SettingItemTextField(
    icon: ImageVector,
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1E37), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xFF7C3AED),
            modifier = Modifier.size(24.dp)
        )
        
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, color = Color(0xFF00D4FF), fontSize = 12.sp)
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFF0A0E27),
                    focusedContainerColor = Color(0xFF0A0E27),
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White
                ),
                singleLine = true
            )
        }
    }
}

@Composable
fun SettingItemSlider(
    icon: ImageVector,
    label: String,
    value: Int,
    range: IntRange,
    unit: String,
    onValueChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1E37), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color(0xFF7C3AED),
                    modifier = Modifier.size(20.dp)
                )
                Text(text = label, color = Color(0xFF00D4FF), fontSize = 12.sp)
            }
            
            Text(
                text = "$value$unit",
                color = Color(0xFF10B981),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF00D4FF),
                activeTrackColor = Color(0xFF00D4FF),
                inactiveTrackColor = Color(0xFF0A0E27)
            )
        )
    }
}

@Composable
fun SettingItemSliderFloat(
    icon: ImageVector,
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1E37), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color(0xFF7C3AED),
                    modifier = Modifier.size(20.dp)
                )
                Text(text = label, color = Color(0xFF00D4FF), fontSize = 12.sp)
            }
            
            Text(
                text = "${"%.2f".format(value)}",
                color = Color(0xFF10B981),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF00D4FF),
                activeTrackColor = Color(0xFF00D4FF),
                inactiveTrackColor = Color(0xFF0A0E27)
            )
        )
    }
}

@Composable
fun SettingItemToggle(
    icon: ImageVector,
    label: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1E37), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xFF7C3AED),
            modifier = Modifier.size(24.dp)
        )
        
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, color = Color(0xFF00D4FF), fontSize = 12.sp)
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                options.forEach { option ->
                    Button(
                        onClick = { onValueChange(option) },
                        modifier = Modifier.height(28.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (value == option) Color(0xFF00D4FF) else Color(0xFF0A0E27)
                        ),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = option,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (value == option) Color(0xFF0A0E27) else Color(0xFF00D4FF)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingItemToggleSwitch(
    icon: ImageVector,
    label: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1E37), RoundedCornerShape(8.dp))
            .padding(12.dp)
            .clickable { onCheckedChange(!isChecked) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFF7C3AED),
                modifier = Modifier.size(24.dp)
            )
            Text(text = label, color = Color(0xFF00D4FF), fontSize = 12.sp)
        }
        
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF00D4FF),
                checkedTrackColor = Color(0xFF00D4FF).copy(alpha = 0.3f),
                uncheckedThumbColor = Color(0xFF7C3AED),
                uncheckedTrackColor = Color(0xFF1A1E37)
            )
        )
    }
}

@Composable
fun SettingItemButton(
    icon: ImageVector,
    label: String,
    description: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF1A1E37)
        ),
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFF7C3AED),
                modifier = Modifier.size(28.dp)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    color = Color(0xFF00D4FF),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    color = Color(0xFF7C3AED),
                    fontSize = 11.sp
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = Color(0xFF7C3AED),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun SettingItemStatus(
    icon: ImageVector,
    label: String,
    status: String,
    statusColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1E37), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFF7C3AED),
                modifier = Modifier.size(24.dp)
            )
            Text(text = label, color = Color(0xFF00D4FF), fontSize = 12.sp)
        }
        
        Box(
            modifier = Modifier
                .background(statusColor.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = status,
                color = statusColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SettingItemInfo(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1E37), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color(0xFF00D4FF), fontSize = 12.sp)
        Text(
            text = value,
            color = Color(0xFF10B981),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SettingItemLink(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1E37), RoundedCornerShape(8.dp))
            .clickable { }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color(0xFF00D4FF), fontSize = 12.sp)
        Text(
            text = value,
            color = Color(0xFF7C3AED),
            fontSize = 11.sp
        )
    }
}

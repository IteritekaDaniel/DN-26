package com.example.dn_26

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dn_26.ai.AIService
import com.example.dn_26.alert.AlertEngine
import com.example.dn_26.alert.AlertThresholds
import com.example.dn_26.data.local.DroneDatabase
import com.example.dn_26.data.remote.ApiClient
import com.example.dn_26.data.repository.AIRepository
import com.example.dn_26.data.repository.AlertRepository
import com.example.dn_26.data.repository.DroneRepository
import com.example.dn_26.data.repository.TelemetryRepository
import com.example.dn_26.domain.model.ConnectionProfile
import com.example.dn_26.domain.model.DroneCommand
import com.example.dn_26.drone.HybridDroneController
import com.example.dn_26.presentation.ui.components.TopParameterBar
import com.example.dn_26.presentation.ui.gamepad.PhysicalGamepadHandler
import com.example.dn_26.presentation.ui.screen.AIAnalysisScreen
import com.example.dn_26.presentation.ui.screen.AdvancedFlightControlScreen
import com.example.dn_26.presentation.ui.screen.DataAnalyticsScreen
import com.example.dn_26.presentation.ui.screen.EnergyManagementScreen
import com.example.dn_26.presentation.ui.screen.FleetManagementScreen
import com.example.dn_26.presentation.ui.screen.FlightMapScreen
import com.example.dn_26.presentation.ui.screen.MissionControlDashboardScreen
import com.example.dn_26.presentation.ui.screen.MissionLogsScreen
import com.example.dn_26.presentation.ui.screen.SettingsProScreen
import com.example.dn_26.presentation.ui.theme.DroneXColors
import com.example.dn_26.presentation.ui.theme.DroneXProTheme
import com.example.dn_26.presentation.viewmodel.AIViewModel
import com.example.dn_26.presentation.viewmodel.AlertViewModel
import com.example.dn_26.presentation.viewmodel.DroneControlViewModel
import com.example.dn_26.presentation.viewmodel.TelemetryViewModel

class MainActivity : ComponentActivity() {

    private lateinit var droneControlViewModel: DroneControlViewModel
    private lateinit var telemetryViewModel: TelemetryViewModel
    private lateinit var alertViewModel: AlertViewModel
    private lateinit var aiViewModel: AIViewModel

    private val gamepadHandler = PhysicalGamepadHandler()
    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        requestRuntimePermissions()
        setupDependencies()

        gamepadHandler.onCommand = { command, _ ->
            droneControlViewModel.executeCommand(command)
        }
        gamepadHandler.onMovement = { x, y, z, rotation ->
            droneControlViewModel.updateJoystickInput(x, y, z, rotation)
        }

        setContent {
            DroneXProTheme {
                MainCommandCenter()
            }
        }
    }

    override fun onGenericMotionEvent(event: MotionEvent?): Boolean {
        event?.let {
            return gamepadHandler.onGenericMotionEvent(it)
        }
        return super.onGenericMotionEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return gamepadHandler.onKeyDown(keyCode) ||
            super.onKeyDown(keyCode, event)
    }

    @Composable
    fun MainCommandCenter() {
        var currentScreen by remember { mutableIntStateOf(0) }
        val droneControlState by droneControlViewModel.state.collectAsState()
        val telemetryState by telemetryViewModel.state.collectAsState()
        val alertState by alertViewModel.state.collectAsState()
        val aiState by aiViewModel.state.collectAsState()

        LaunchedEffect(telemetryState.sampleCount) {
            telemetryState.latestTelemetry?.let { telemetry ->
                alertViewModel.evaluateTelemetry(telemetry)
                if (telemetryState.sampleCount % 20 == 0) {
                    aiViewModel.analyzeTelemetry(telemetry, telemetryState.recentTelemetry)
                }
            }
        }

        Row(modifier = Modifier.fillMaxSize()) {
            NavigationRail(
                containerColor = Color.Black.copy(alpha = 0.94f),
                contentColor = DroneXColors.PrimaryAccent,
                modifier = Modifier.fillMaxHeight(),
                header = {
                    Icon(
                        imageVector = Icons.Default.Radar,
                        contentDescription = null,
                        tint = DroneXColors.PrimaryAccent,
                        modifier = Modifier.padding(vertical = 24.dp).size(36.dp)
                    )
                }
            ) {
                NavigationItem(currentScreen == 0, { currentScreen = 0 }, Icons.Default.Dashboard, "DASH")
                NavigationItem(currentScreen == 1, { currentScreen = 1 }, Icons.Default.Gamepad, "PILOT")
                NavigationItem(currentScreen == 2, { currentScreen = 2 }, Icons.Default.AutoAwesome, "AI")
                NavigationItem(currentScreen == 3, { currentScreen = 3 }, Icons.Default.Analytics, "DATA")
                NavigationItem(currentScreen == 4, { currentScreen = 4 }, Icons.Default.Map, "MAP")
                NavigationItem(currentScreen == 5, { currentScreen = 5 }, Icons.Default.RocketLaunch, "FLEET")
                NavigationItem(currentScreen == 6, { currentScreen = 6 }, Icons.Default.BatteryChargingFull, "POWER")
                NavigationItem(currentScreen == 7, { currentScreen = 7 }, Icons.AutoMirrored.Filled.Assignment, "LOGS")

                Spacer(Modifier.weight(1f))

                NavigationItem(currentScreen == 8, { currentScreen = 8 }, Icons.Default.Settings, "SYS")
            }

            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                TopParameterBar(
                    telemetry = telemetryState.latestTelemetry,
                    droneState = droneControlState,
                    onQuickSettingsClick = { currentScreen = 8 }
                )

                Box(modifier = Modifier.fillMaxSize()) {
                    when (currentScreen) {
                        0 -> MissionControlDashboardScreen(
                            droneState = droneControlState,
                            telemetryState = telemetryState,
                            alertState = alertState,
                            aiState = aiState,
                            onConnect = { profile: ConnectionProfile ->
                                droneControlViewModel.updateConnectionProfile(
                                    mode = profile.mode,
                                    ipAddress = profile.ipAddress,
                                    port = profile.port,
                                    bluetoothAddress = profile.bluetoothAddress,
                                    bluetoothName = profile.bluetoothName
                                )
                                droneControlViewModel.connectDrone(profile)
                            },
                            onDisconnect = { droneControlViewModel.disconnectDrone() },
                            onCommand = { command: DroneCommand ->
                                if (command == DroneCommand.EMERGENCY_STOP) {
                                    droneControlViewModel.emergencyStop()
                                } else {
                                    droneControlViewModel.executeCommand(command)
                                }
                            },
                            onNavigateToControl = { currentScreen = 1 },
                            onNavigateToMap = { currentScreen = 4 },
                            onNavigateToAnalytics = { currentScreen = 3 },
                            onNavigateToSettings = { currentScreen = 8 }
                        )
                        1 -> AdvancedFlightControlScreen(
                            droneState = droneControlState,
                            telemetry = telemetryState.latestTelemetry,
                            onNavigateBack = { currentScreen = 0 },
                            onCommand = { command ->
                                if (command == DroneCommand.EMERGENCY_STOP) {
                                    droneControlViewModel.emergencyStop()
                                } else {
                                    droneControlViewModel.executeCommand(command)
                                }
                            },
                            onEmergencyStop = { droneControlViewModel.emergencyStop() },
                            onUnlockSafety = { droneControlViewModel.unlockSafety() },
                            onFlightModeChange = { droneControlViewModel.setFlightMode(it) },
                            onControllerPresetChange = { droneControlViewModel.setControllerPreset(it) },
                            onControllerTuningChange = { sensitivity, deadZone, expo ->
                                droneControlViewModel.updateControllerTuning(sensitivity, deadZone, expo)
                            },
                            onSendJoystick = { x, y, z, rotation ->
                                droneControlViewModel.updateJoystickInput(x, y, z, rotation)
                            }
                        )
                        2 -> AIAnalysisScreen(
                            aiViewModel = aiViewModel,
                            latestTelemetry = telemetryState.latestTelemetry,
                            recentTelemetry = telemetryState.recentTelemetry
                        )
                        3 -> DataAnalyticsScreen(samples = telemetryState.recentTelemetry)
                        4 -> FlightMapScreen(onNavigateBack = { currentScreen = 0 })
                        5 -> FleetManagementScreen()
                        6 -> EnergyManagementScreen()
                        7 -> MissionLogsScreen(alertViewModel)
                        8 -> SettingsProScreen(
                            onNavigateBack = { currentScreen = 0 },
                            connectionProfile = droneControlState.connectionProfile,
                            onConnectWifi = { profile ->
                                droneControlViewModel.updateConnectionProfile(
                                    mode = profile.mode,
                                    ipAddress = profile.ipAddress,
                                    port = profile.port,
                                    bluetoothAddress = profile.bluetoothAddress,
                                    bluetoothName = profile.bluetoothName
                                )
                                droneControlViewModel.connectDrone(profile)
                                currentScreen = 0
                            },
                            onCalibrate = { droneControlViewModel.executeCommand(DroneCommand.CALIBRATE) }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun ColumnScope.NavigationItem(
        selected: Boolean,
        onClick: () -> Unit,
        icon: ImageVector,
        label: String
    ) {
        NavigationRailItem(
            selected = selected,
            onClick = onClick,
            icon = { Icon(icon, contentDescription = label, modifier = Modifier.size(24.dp)) },
            label = { Text(label, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp) },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = DroneXColors.PrimaryAccent,
                selectedTextColor = DroneXColors.PrimaryAccent,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = DroneXColors.PrimaryAccent.copy(alpha = 0.12f)
            )
        )
    }

    private fun setupDependencies() {
        val database = DroneDatabase.getDatabase(this)
        val droneController = HybridDroneController(applicationContext)
        val droneRepo = DroneRepository(droneController)
        val alertRepo = AlertRepository(database.alertDao())
        val telemetryRepo = TelemetryRepository(droneRepo, database.telemetryDao())
        val aiRepo = AIRepository(ApiClient.getAIApiService())
        val aiService = AIService(aiRepo)

        droneControlViewModel = DroneControlViewModel(droneRepo, alertRepo)
        telemetryViewModel = TelemetryViewModel(telemetryRepo)
        alertViewModel = AlertViewModel(
            alertRepo,
            AlertEngine(alertRepo, AlertThresholds(15, 30, 70.0, 55.0, 10, 15.0))
        )
        aiViewModel = AIViewModel(aiService)

        telemetryViewModel.startTelemetryObservation()
    }

    private fun requestRuntimePermissions() {
        val permissions = buildList {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            add(Manifest.permission.CAMERA)
            add(Manifest.permission.RECORD_AUDIO)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_SCAN)
                add(Manifest.permission.BLUETOOTH_CONNECT)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.toTypedArray()

        permissionsLauncher.launch(permissions)
    }
}

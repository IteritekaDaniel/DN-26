package com.example.dn_26.presentation.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dn_26.presentation.ui.theme.DroneXColors

/**
 * 🏠 HOME PAGE BOTTOM NAVIGATION v2.0
 * 
 * Features:
 * ✅ Glassmorphic design
 * ✅ Animated selection indicator
 * ✅ Professional Iconography
 * ✅ Haptic-ready feedback layout
 */

@Composable
fun ProfessionalBottomNavBar(
    currentScreen: Int,
    onScreenSelected: (Int) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(72.dp)
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = DroneXColors.PrimaryDark,
                spotColor = DroneXColors.PrimaryDark
            ),
        color = DroneXColors.SurfaceDark.copy(alpha = 0.95f),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            DroneXColors.PrimaryDark.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                selected = currentScreen == 0,
                icon = Icons.Default.Dashboard,
                label = "Home",
                onClick = { onScreenSelected(0) }
            )
            BottomNavItem(
                selected = currentScreen == 1,
                icon = Icons.Default.Gamepad,
                label = "Pilot",
                onClick = { onScreenSelected(1) }
            )
            
            // Central Takeoff/Action Button
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(DroneXColors.PrimaryDark)
                    .clickable { onScreenSelected(4) }, // Shortcut to Map/Plan
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.RocketLaunch,
                    contentDescription = "Quick Action",
                    tint = DroneXColors.BackgroundDark,
                    modifier = Modifier.size(28.dp)
                )
            }

            BottomNavItem(
                selected = currentScreen == 3,
                icon = Icons.Default.Analytics,
                label = "Stats",
                onClick = { onScreenSelected(3) }
            )
            BottomNavItem(
                selected = currentScreen == 8,
                icon = Icons.Default.Settings,
                label = "System",
                onClick = { onScreenSelected(8) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) DroneXColors.PrimaryDark else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        AnimatedVisibility(
            visible = selected,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = label,
                color = DroneXColors.PrimaryDark,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

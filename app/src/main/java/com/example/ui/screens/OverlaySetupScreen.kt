package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameProfile
import com.example.service.FloatingHudService
import com.example.service.TouchAccessibilityService
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun OverlaySetupScreen(
    activeProfile: GameProfile,
    isFloatingRunning: Boolean,
    onToggleFloatingService: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasOverlayPermission by remember { mutableStateOf(checkOverlayPermission(context)) }
    var isAccessibilityServiceEnabled by remember { mutableStateOf(TouchAccessibilityService.isConnected()) }

    // Periodically poll permissions in case user returns from system settings
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            hasOverlayPermission = checkOverlayPermission(context)
            isAccessibilityServiceEnabled = TouchAccessibilityService.isConnected()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("overlay_setup_screen")
    ) {
        // Header
        Text(
            text = "Game Overlay Center",
            color = NeonCyan,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = "Play your mobile games with floating virtual console controls",
            color = TextSecondary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Big Master Switch Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            if (isFloatingRunning) NeonGreen else NeonCyan,
                            if (isFloatingRunning) NeonCyan else NeonPink
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = CyberCard)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Glow status ring
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(if (isFloatingRunning) NeonGreen.copy(alpha = 0.2f) else CyberSurface)
                        .border(2.dp, if (isFloatingRunning) NeonGreen else CyberCardBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isFloatingRunning) Icons.Default.PlayArrow else Icons.Default.Layers,
                        contentDescription = null,
                        tint = if (isFloatingRunning) NeonGreen else NeonCyan,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (isFloatingRunning) "FLOATING HUD ACTIVE" else "FLOATING HUD OFFLINE",
                    color = if (isFloatingRunning) NeonGreen else TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = if (isFloatingRunning) {
                        "Virtual controls are currently floating over your screen with ${activeProfile.name}"
                    } else {
                        "Start overlay to display custom console buttons over your games"
                    },
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (!hasOverlayPermission) {
                            requestOverlayPermission(context)
                        } else {
                            onToggleFloatingService()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("toggle_floating_service_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFloatingRunning) NeonRed else NeonCyan,
                        contentColor = if (isFloatingRunning) Color.White else Color(0xFF031622)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = if (isFloatingRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isFloatingRunning) "STOP FLOATING HUD" else "START FLOATING HUD",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // System Permissions Checklist
        Text(
            text = "Required Permissions",
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))

        // 1. Overlay Permission Card
        PermissionStatusCard(
            title = "1. Draw Over Other Apps",
            description = "Allows the virtual gamepad HUD to hover transparently on top of any game.",
            isGranted = hasOverlayPermission,
            actionLabel = "Grant Permission",
            onAction = { requestOverlayPermission(context) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Touch Injection Accessibility Service Card
        PermissionStatusCard(
            title = "2. Touch Injection Service",
            description = "Allows HUD buttons to simulate taps and swipes at mapped game coordinates.",
            isGranted = isAccessibilityServiceEnabled,
            actionLabel = "Enable Service",
            onAction = {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                context.startActivity(intent)
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // How to use guide
        Text(
            text = "How to Play with Custom HUD",
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))

        val steps = listOf(
            "1. Start the Floating HUD above.",
            "2. Switch to your game (e.g. PUBG, Genshin, Roblox, Emulators).",
            "3. Tap the floating ConsoleHUD bubble to expand the quick dock.",
            "4. Tap 'MOVE' to drag buttons onto the game's touch spots.",
            "5. Tap 'LOCK' to lock controls in place and start gaming!"
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, CyberCardBorder, RoundedCornerShape(16.dp)),
            color = CyberSurface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                steps.forEach { step ->
                    Text(
                        text = step,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionStatusCard(
    title: String,
    description: String,
    isGranted: Boolean,
    actionLabel: String,
    onAction: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, if (isGranted) NeonGreen.copy(alpha = 0.5f) else CyberCardBorder, RoundedCornerShape(14.dp)),
        color = CyberCard
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(if (isGranted) NeonGreen.copy(alpha = 0.2f) else NeonPink.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isGranted) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (isGranted) NeonGreen else NeonPink,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = description, color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            if (!isGranted) {
                OutlinedButton(
                    onClick = onAction,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(actionLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NeonGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("ACTIVE", color = NeonGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

fun checkOverlayPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Settings.canDrawOverlays(context)
    } else {
        true
    }
}

fun requestOverlayPermission(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        )
        context.startActivity(intent)
    }
}

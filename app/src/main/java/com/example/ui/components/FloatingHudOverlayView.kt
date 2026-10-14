package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameProfile
import com.example.model.HudButtonConfig
import com.example.model.HudButtonType
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.roundToInt

@Composable
fun FloatingHudOverlayView(
    profileFlow: StateFlow<GameProfile?>,
    isEditModeFlow: StateFlow<Boolean>,
    isHudVisibleFlow: StateFlow<Boolean>,
    onToggleEditMode: () -> Unit,
    onToggleVisibility: () -> Unit,
    onCloseService: () -> Unit,
    onButtonTriggered: (HudButtonConfig) -> Unit,
    onButtonPositionUpdated: (HudButtonConfig) -> Unit
) {
    val profile by profileFlow.collectAsState()
    val isEditMode by isEditModeFlow.collectAsState()
    val isHudVisible by isHudVisibleFlow.collectAsState()

    var isDockExpanded by remember { mutableStateOf(false) }
    var dockOffsetX by remember { mutableFloatStateOf(20f) }
    var dockOffsetY by remember { mutableFloatStateOf(100f) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val screenWidthPx = with(density) { maxWidth.toPx() }
        val screenHeightPx = with(density) { maxHeight.toPx() }

        val active = profile
        if (active != null && isHudVisible) {
            // Render HUD buttons
            active.buttons.forEach { button ->
                val btnXPx = button.hudXRatio * screenWidthPx
                val btnYPx = button.hudYRatio * screenHeightPx

                val btnSizeDp = button.sizeDp.dp
                val halfSizePx = with(density) { (button.sizeDp / 2).dp.toPx() }

                Box(
                    modifier = Modifier.offset {
                        IntOffset(
                            (btnXPx - halfSizePx).roundToInt(),
                            (btnYPx - halfSizePx).roundToInt()
                        )
                    }
                ) {
                    if (button.type == HudButtonType.ANALOG_STICK || button.type == HudButtonType.ANALOG_STICK_RIGHT) {
                        AnalogStickView(
                            button = button,
                            theme = active.theme,
                            isEditMode = isEditMode,
                            onDirectionChanged = { dx, dy ->
                                onButtonTriggered(button)
                            },
                            onPositionChanged = { deltaX, deltaY ->
                                val newXRatio = ((btnXPx + deltaX) / screenWidthPx).coerceIn(0.05f, 0.95f)
                                val newYRatio = ((btnYPx + deltaY) / screenHeightPx).coerceIn(0.05f, 0.95f)
                                onButtonPositionUpdated(button.copy(hudXRatio = newXRatio, hudYRatio = newYRatio))
                            }
                        )
                    } else {
                        HudButtonView(
                            button = button,
                            theme = active.theme,
                            isEditMode = isEditMode,
                            onPress = { onButtonTriggered(button) },
                            onPositionChanged = { deltaX, deltaY ->
                                val newXRatio = ((btnXPx + deltaX) / screenWidthPx).coerceIn(0.05f, 0.95f)
                                val newYRatio = ((btnYPx + deltaY) / screenHeightPx).coerceIn(0.05f, 0.95f)
                                onButtonPositionUpdated(button.copy(hudXRatio = newXRatio, hudYRatio = newYRatio))
                            }
                        )
                    }
                }
            }
        }

        // Draggable floating dock pill
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(dockOffsetX.roundToInt(), dockOffsetY.roundToInt())
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        dockOffsetX = (dockOffsetX + dragAmount.x).coerceIn(0f, screenWidthPx - 200f)
                        dockOffsetY = (dockOffsetY + dragAmount.y).coerceIn(40f, screenHeightPx - 200f)
                    }
                }
        ) {
            if (!isDockExpanded) {
                // Minimized bubble button
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .shadow(8.dp, CircleShape, ambientColor = NeonCyan, spotColor = NeonCyan)
                        .clip(CircleShape)
                        .background(CyberBackground.copy(alpha = 0.9f))
                        .border(1.5.dp, if (isEditMode) NeonPink else NeonCyan, CircleShape)
                        .clickable { isDockExpanded = true }
                        .testTag("floating_dock_bubble"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = "ConsoleHUD Dock",
                        tint = if (isEditMode) NeonPink else NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                // Expanded Quick Control Bar
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.5.dp, CyberCardBorder, RoundedCornerShape(16.dp)),
                    color = CyberBackground.copy(alpha = 0.95f),
                    shadowElevation = 10.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Title / Profile Tag
                        Column(modifier = Modifier.padding(start = 4.dp, end = 4.dp)) {
                            Text(
                                text = "ConsoleHUD",
                                color = NeonCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = active?.name?.take(14) ?: "Profile",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }

                        // Toggle Edit Mode
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isEditMode) NeonPink.copy(alpha = 0.25f) else CyberCard)
                                .border(
                                    width = 1.dp,
                                    color = if (isEditMode) NeonPink else CyberCardBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onToggleEditMode() }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isEditMode) Icons.Default.Lock else Icons.Default.Edit,
                                    contentDescription = "Edit Mode",
                                    tint = if (isEditMode) NeonPink else TextPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isEditMode) "LOCK" else "MOVE",
                                    color = if (isEditMode) NeonPink else TextPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Toggle Visibility
                        IconButton(
                            onClick = onToggleVisibility,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isHudVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Visibility",
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Minimize to bubble
                        IconButton(
                            onClick = { isDockExpanded = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Minimize Dock",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Close overlay service
                        IconButton(
                            onClick = onCloseService,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Floating HUD",
                                tint = NeonPink,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

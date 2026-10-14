package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameProfile
import com.example.model.HudButtonConfig
import com.example.model.HudButtonType
import com.example.model.TouchTelemetry
import com.example.ui.components.AnalogStickView
import com.example.ui.components.ButtonInspectorDialog
import com.example.ui.components.HudButtonView
import com.example.ui.components.MappingReticleOverlay
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

enum class StudioMode {
    PLAY,
    EDIT_HUD,
    MAP_RETICLE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HudStudioScreen(
    profile: GameProfile,
    allProfiles: List<GameProfile>,
    onSelectProfile: (String) -> Unit,
    onUpdateButton: (HudButtonConfig) -> Unit,
    onAddButton: (HudButtonConfig) -> Unit,
    onDeleteButton: (String) -> Unit,
    onResetDefaults: () -> Unit,
    onButtonPress: (HudButtonConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    var mode by remember { mutableStateOf(StudioMode.PLAY) }
    var selectedButtonId by remember { mutableStateOf<String?>(null) }
    var buttonToEdit by remember { mutableStateOf<HudButtonConfig?>(null) }
    var showAddButtonSheet by remember { mutableStateOf(false) }
    var showProfileDropdown by remember { mutableStateOf(false) }
    var lastTelemetry by remember { mutableStateOf<TouchTelemetry?>(null) }

    val selectedButton = profile.buttons.find { it.id == selectedButtonId }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
    ) {
        val density = LocalDensity.current
        val screenWidthPx = with(density) { maxWidth.toPx() }
        val screenHeightPx = with(density) { maxHeight.toPx() }

        // Subtle background gaming blueprint grid
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF131D33),
                            CyberBackground
                        ),
                        radius = screenWidthPx
                    )
                )
        )

        // HUD Controls Layer
        profile.buttons.forEach { button ->
            val btnXPx = button.hudXRatio * screenWidthPx
            val btnYPx = button.hudYRatio * screenHeightPx
            val halfSizePx = with(density) { (button.sizeDp / 2).dp.toPx() }
            val isSelected = button.id == selectedButtonId

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
                        theme = profile.theme,
                        isEditMode = mode == StudioMode.EDIT_HUD,
                        isSelected = isSelected,
                        onDirectionChanged = { dx, dy ->
                            onButtonPress(button)
                            lastTelemetry = TouchTelemetry(button.id, button.name, dx, dy)
                        },
                        onPositionChanged = { deltaX, deltaY ->
                            val newX = ((btnXPx + deltaX) / screenWidthPx).coerceIn(0.05f, 0.95f)
                            val newY = ((btnYPx + deltaY) / screenHeightPx).coerceIn(0.05f, 0.95f)
                            onUpdateButton(button.copy(hudXRatio = newX, hudYRatio = newY))
                        },
                        onSelectForEdit = {
                            selectedButtonId = button.id
                            if (mode == StudioMode.EDIT_HUD) {
                                buttonToEdit = button
                            }
                        }
                    )
                } else {
                    HudButtonView(
                        button = button,
                        theme = profile.theme,
                        isEditMode = mode == StudioMode.EDIT_HUD,
                        isSelected = isSelected,
                        onPress = {
                            onButtonPress(button)
                            lastTelemetry = TouchTelemetry(button.id, button.name, button.targetXRatio, button.targetYRatio)
                        },
                        onPositionChanged = { deltaX, deltaY ->
                            val newX = ((btnXPx + deltaX) / screenWidthPx).coerceIn(0.05f, 0.95f)
                            val newY = ((btnYPx + deltaY) / screenHeightPx).coerceIn(0.05f, 0.95f)
                            onUpdateButton(button.copy(hudXRatio = newX, hudYRatio = newY))
                        },
                        onSelectForEdit = {
                            selectedButtonId = button.id
                            if (mode == StudioMode.EDIT_HUD) {
                                buttonToEdit = button
                            }
                        }
                    )
                }
            }
        }

        // Mapping Reticle Crosshairs Layer (Active in MAP_RETICLE mode)
        if (mode == StudioMode.MAP_RETICLE) {
            MappingReticleOverlay(
                buttons = profile.buttons,
                screenWidthPx = screenWidthPx,
                screenHeightPx = screenHeightPx,
                selectedButtonId = selectedButtonId,
                onTargetPositionChanged = { btnId, deltaX, deltaY ->
                    val btn = profile.buttons.find { it.id == btnId } ?: return@MappingReticleOverlay
                    val curTargetX = btn.targetXRatio * screenWidthPx
                    val curTargetY = btn.targetYRatio * screenHeightPx
                    val newX = ((curTargetX + deltaX) / screenWidthPx).coerceIn(0.02f, 0.98f)
                    val newY = ((curTargetY + deltaY) / screenHeightPx).coerceIn(0.02f, 0.98f)
                    onUpdateButton(btn.copy(targetXRatio = newX, targetYRatio = newY))
                },
                onSelectButton = { btnId ->
                    selectedButtonId = btnId
                }
            )
        }

        // Top HUD Header Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .align(Alignment.TopCenter)
        ) {
            // Profile & Actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Selector Pill
                Box {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .clickable { showProfileDropdown = true }
                            .testTag("profile_selector_dropdown"),
                        color = CyberCard.copy(alpha = 0.9f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🎮 ${profile.name.take(18)}",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "▾", color = NeonCyan, fontSize = 12.sp)
                        }
                    }

                    DropdownMenu(
                        expanded = showProfileDropdown,
                        onDismissRequest = { showProfileDropdown = false }
                    ) {
                        allProfiles.forEach { p ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(p.name, fontWeight = FontWeight.Bold)
                                        Text(p.gameCategory, fontSize = 11.sp, color = TextSecondary)
                                    }
                                },
                                onClick = {
                                    onSelectProfile(p.id)
                                    showProfileDropdown = false
                                }
                            )
                        }
                    }
                }

                // Add Button & Reset Defaults
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { showAddButtonSheet = true },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(NeonCyan.copy(alpha = 0.15f))
                            .border(1.dp, NeonCyan, CircleShape)
                            .testTag("add_hud_button_action")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Button", tint = NeonCyan, modifier = Modifier.size(18.dp))
                    }

                    IconButton(
                        onClick = onResetDefaults,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CyberCard)
                            .border(1.dp, CyberCardBorder, CircleShape)
                            .testTag("reset_layout_button")
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Reset Layout", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Studio Mode Switcher Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, CyberCardBorder, RoundedCornerShape(14.dp)),
                color = CyberSurface.copy(alpha = 0.85f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    StudioMode.values().forEach { m ->
                        val isCurrent = mode == m
                        val modeColor = when (m) {
                            StudioMode.PLAY -> NeonGreen
                            StudioMode.EDIT_HUD -> NeonCyan
                            StudioMode.MAP_RETICLE -> NeonPink
                        }
                        val title = when (m) {
                            StudioMode.PLAY -> "PLAY TEST"
                            StudioMode.EDIT_HUD -> "EDIT HUD"
                            StudioMode.MAP_RETICLE -> "MAP RETICLE"
                        }
                        val icon = when (m) {
                            StudioMode.PLAY -> Icons.Default.PlayArrow
                            StudioMode.EDIT_HUD -> Icons.Default.Edit
                            StudioMode.MAP_RETICLE -> Icons.Default.AdsClick
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isCurrent) modeColor.copy(alpha = 0.2f) else Color.Transparent)
                                .border(
                                    width = if (isCurrent) 1.5.dp else 0.dp,
                                    color = if (isCurrent) modeColor else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { mode = m }
                                .padding(vertical = 8.dp)
                                .testTag("studio_mode_${m.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = title,
                                    tint = if (isCurrent) modeColor else TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = title,
                                    color = if (isCurrent) modeColor else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Mode hint banner
            Spacer(modifier = Modifier.height(6.dp))
            val hintText = when (mode) {
                StudioMode.PLAY -> "🎮 Press buttons to test tactile haptic rumble, audio and glow."
                StudioMode.EDIT_HUD -> "✏️ Drag buttons to re-position on screen. Tap to customize size & turbo."
                StudioMode.MAP_RETICLE -> "🎯 Drag the target crosshairs to where the game controls appear on screen."
            }
            Text(
                text = hintText,
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // Bottom Telemetry Live Bar
        if (lastTelemetry != null) {
            val t = lastTelemetry!!
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0C1322).copy(alpha = 0.92f))
                    .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(NeonGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "INPUT: ${t.buttonName} | TARGET: (${(t.screenX * 100).roundToInt()}%, ${(t.screenY * 100).roundToInt()}%)",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Button Inspector Dialog
        if (buttonToEdit != null) {
            ButtonInspectorDialog(
                button = buttonToEdit!!,
                onDismiss = { buttonToEdit = null },
                onSave = { updated ->
                    onUpdateButton(updated)
                    buttonToEdit = null
                },
                onDelete = { btnId ->
                    onDeleteButton(btnId)
                    buttonToEdit = null
                    selectedButtonId = null
                }
            )
        }

        // Add Button Bottom Sheet
        if (showAddButtonSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAddButtonSheet = false },
                containerColor = CyberSurface
            ) {
                AddButtonSheetContent(
                    onAdd = { newButton ->
                        onAddButton(newButton)
                        showAddButtonSheet = false
                    }
                )
            }
        }
    }
}

@Composable
fun AddButtonSheetContent(
    onAdd: (HudButtonConfig) -> Unit
) {
    val buttonTemplates = listOf(
        Pair("Action A", HudButtonType.ACTION_A),
        Pair("Action B", HudButtonType.ACTION_B),
        Pair("Action X", HudButtonType.ACTION_X),
        Pair("Action Y", HudButtonType.ACTION_Y),
        Pair("Trigger L2", HudButtonType.TRIGGER_L2),
        Pair("Trigger R2", HudButtonType.TRIGGER_R2),
        Pair("Bumper L1", HudButtonType.BUMPER_L1),
        Pair("Bumper R1", HudButtonType.BUMPER_R1),
        Pair("Analog Stick", HudButtonType.ANALOG_STICK),
        Pair("Aim Stick", HudButtonType.ANALOG_STICK_RIGHT),
        Pair("Skill 1", HudButtonType.SKILL_1),
        Pair("Skill 2", HudButtonType.SKILL_2),
        Pair("Ultimate", HudButtonType.SKILL_ULTIMATE),
        Pair("Reload", HudButtonType.ACTION_RELOAD),
        Pair("Jump", HudButtonType.ACTION_JUMP),
        Pair("Heal", HudButtonType.ACTION_HEAL),
        Pair("Start", HudButtonType.BUTTON_START),
        Pair("Select", HudButtonType.BUTTON_SELECT)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Text(
            text = "Add Console Control",
            color = NeonCyan,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Select a control to add to your HUD canvas",
            color = TextSecondary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(buttonTemplates) { (name, type) ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberCard)
                        .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp))
                        .clickable {
                            val newBtn = HudButtonConfig(
                                id = "btn_${type.name.lowercase()}_${System.currentTimeMillis() % 10000}",
                                name = name,
                                type = type,
                                hudXRatio = 0.5f,
                                hudYRatio = 0.5f,
                                targetXRatio = 0.5f,
                                targetYRatio = 0.5f,
                                sizeDp = if (type == HudButtonType.ANALOG_STICK || type == HudButtonType.ANALOG_STICK_RIGHT) 100f else 60f
                            )
                            onAdd(newBtn)
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                        .testTag("add_template_${type.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = name, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ButtonShape
import com.example.model.HudButtonConfig
import com.example.model.HudButtonType
import com.example.model.HudTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.PsCircle
import com.example.ui.theme.PsCross
import com.example.ui.theme.PsSquare
import com.example.ui.theme.PsTriangle
import com.example.ui.theme.XboxBlue
import com.example.ui.theme.XboxGreen
import com.example.ui.theme.XboxRed
import com.example.ui.theme.XboxYellow
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun HudButtonView(
    button: HudButtonConfig,
    theme: HudTheme,
    isEditMode: Boolean,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier,
    onPress: () -> Unit = {},
    onPositionChanged: (Float, Float) -> Unit = { _, _ -> },
    onSelectForEdit: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Handle Turbo repeat firing while pressed
    if (button.isTurbo && isPressed && !isEditMode) {
        LaunchedEffect(Unit) {
            while (true) {
                delay(85)
                onPress()
            }
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else if (isSelected) 1.08f else 1.0f,
        label = "button_scale"
    )

    val buttonColor = resolveButtonColor(button, theme)
    val buttonShape = when (button.shape) {
        ButtonShape.CIRCLE -> CircleShape
        ButtonShape.ROUNDED_SQUARE -> RoundedCornerShape(14.dp)
        ButtonShape.PILL -> RoundedCornerShape(24.dp)
        ButtonShape.OCTAGON -> RoundedCornerShape(18.dp)
    }

    val displayLabel = if (button.customLabel.isNotBlank()) {
        button.customLabel
    } else {
        resolveDefaultLabel(button.type, theme)
    }

    Box(
        modifier = modifier
            .size(button.sizeDp.dp)
            .scale(scale)
            .testTag("hud_button_${button.id}")
            .then(
                if (isEditMode) {
                    Modifier.pointerInput(button.id) {
                        detectDragGestures(
                            onDragStart = { onSelectForEdit() },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onPositionChanged(dragAmount.x, dragAmount.y)
                            }
                        )
                    }
                } else {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onPress() }
                    )
                }
            )
            .shadow(
                elevation = if (isPressed) 2.dp else 6.dp,
                shape = buttonShape,
                ambientColor = buttonColor.copy(alpha = 0.5f),
                spotColor = buttonColor
            )
            .clip(buttonShape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E2640).copy(alpha = button.opacity),
                        Color(0xFF0F1526).copy(alpha = button.opacity)
                    )
                )
            )
            .border(
                width = if (isSelected) 3.dp else if (isPressed) 2.5.dp else 1.5.dp,
                brush = Brush.linearGradient(
                    colors = if (isSelected) {
                        listOf(Color.White, buttonColor)
                    } else if (isPressed) {
                        listOf(buttonColor, Color.White)
                    } else {
                        listOf(buttonColor.copy(alpha = 0.9f), buttonColor.copy(alpha = 0.4f))
                    }
                ),
                shape = buttonShape
            ),
        contentAlignment = Alignment.Center
    ) {
        // Inner glowing core
        if (isPressed) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(buttonColor.copy(alpha = 0.35f))
            )
        }

        // Button Label / Icon symbol
        Text(
            text = displayLabel,
            color = if (isPressed) Color.White else buttonColor,
            fontSize = if (displayLabel.length > 2) 11.sp else 18.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp
        )

        // Turbo or Edit Badge
        if (button.isTurbo) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-3).dp, y = 3.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(NeonPink.copy(alpha = 0.9f))
                    .padding(horizontal = 3.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "T",
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

fun resolveDefaultLabel(type: HudButtonType, theme: HudTheme): String {
    return when (type) {
        HudButtonType.ACTION_A -> when (theme) {
            HudTheme.PLAYSTATION -> "✕"
            HudTheme.SWITCH -> "B"
            else -> "A"
        }
        HudButtonType.ACTION_B -> when (theme) {
            HudTheme.PLAYSTATION -> "○"
            HudTheme.SWITCH -> "A"
            else -> "B"
        }
        HudButtonType.ACTION_X -> when (theme) {
            HudTheme.PLAYSTATION -> "□"
            HudTheme.SWITCH -> "Y"
            else -> "X"
        }
        HudButtonType.ACTION_Y -> when (theme) {
            HudTheme.PLAYSTATION -> "△"
            HudTheme.SWITCH -> "X"
            else -> "Y"
        }
        HudButtonType.DPAD_UP -> "▲"
        HudButtonType.DPAD_DOWN -> "▼"
        HudButtonType.DPAD_LEFT -> "◀"
        HudButtonType.DPAD_RIGHT -> "▶"
        HudButtonType.BUMPER_L1 -> "L1"
        HudButtonType.BUMPER_R1 -> "R1"
        HudButtonType.TRIGGER_L2 -> "L2"
        HudButtonType.TRIGGER_R2 -> "R2"
        HudButtonType.BUTTON_START -> "START"
        HudButtonType.BUTTON_SELECT -> "SELECT"
        HudButtonType.THUMB_L3 -> "L3"
        HudButtonType.THUMB_R3 -> "R3"
        HudButtonType.ANALOG_STICK -> "MOVE"
        HudButtonType.ANALOG_STICK_RIGHT -> "AIM"
        HudButtonType.SKILL_1 -> "SKILL 1"
        HudButtonType.SKILL_2 -> "SKILL 2"
        HudButtonType.SKILL_ULTIMATE -> "ULT"
        HudButtonType.ACTION_RELOAD -> "RELOAD"
        HudButtonType.ACTION_JUMP -> "JUMP"
        HudButtonType.ACTION_HEAL -> "HEAL"
    }
}

fun resolveButtonColor(button: HudButtonConfig, theme: HudTheme): Color {
    if (button.customColorHex.isNotBlank()) {
        try {
            return Color(android.graphics.Color.parseColor(button.customColorHex))
        } catch (e: Exception) {
            // Fallback
        }
    }

    return when (theme) {
        HudTheme.CYBERPUNK -> when (button.type) {
            HudButtonType.ACTION_A, HudButtonType.TRIGGER_R2 -> NeonPink
            HudButtonType.ACTION_B, HudButtonType.BUMPER_R1 -> NeonPurple
            HudButtonType.ACTION_X, HudButtonType.TRIGGER_L2 -> NeonCyan
            HudButtonType.ACTION_Y, HudButtonType.BUMPER_L1 -> Color(0xFFFBBF24)
            HudButtonType.SKILL_ULTIMATE -> Color(0xFFFF2A6D)
            else -> NeonCyan
        }
        HudTheme.PLAYSTATION -> when (button.type) {
            HudButtonType.ACTION_A -> PsCross
            HudButtonType.ACTION_B -> PsCircle
            HudButtonType.ACTION_X -> PsSquare
            HudButtonType.ACTION_Y -> PsTriangle
            else -> Color(0xFF818CF8)
        }
        HudTheme.XBOX -> when (button.type) {
            HudButtonType.ACTION_A -> XboxGreen
            HudButtonType.ACTION_B -> XboxRed
            HudButtonType.ACTION_X -> XboxBlue
            HudButtonType.ACTION_Y -> XboxYellow
            else -> Color(0xFF107C10)
        }
        HudTheme.SWITCH -> when (button.type) {
            HudButtonType.ACTION_A, HudButtonType.ACTION_B, HudButtonType.BUMPER_R1, HudButtonType.TRIGGER_R2 -> Color(0xFFFF3E3E)
            else -> Color(0xFF00C3E3)
        }
        HudTheme.STEALTH -> Color(0xFFE2E8F0)
    }
}

package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HudButtonConfig
import com.example.model.HudTheme
import com.example.ui.theme.NeonCyan
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun AnalogStickView(
    button: HudButtonConfig,
    theme: HudTheme,
    isEditMode: Boolean,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier,
    onDirectionChanged: (x: Float, y: Float) -> Unit = { _, _ -> },
    onPositionChanged: (Float, Float) -> Unit = { _, _ -> },
    onSelectForEdit: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    val baseSizePx = with(density) { button.sizeDp.dp.toPx() }
    val maxRadiusPx = baseSizePx / 2.2f
    val knobSizeDp = (button.sizeDp * 0.45f).dp

    val knobOffsetX = remember { Animatable(0f) }
    val knobOffsetY = remember { Animatable(0f) }

    val accentColor = when (theme) {
        HudTheme.CYBERPUNK -> NeonCyan
        HudTheme.PLAYSTATION -> Color(0xFF3B82F6)
        HudTheme.XBOX -> Color(0xFF107C10)
        HudTheme.SWITCH -> Color(0xFF00C3E3)
        HudTheme.STEALTH -> Color(0xFF94A3B8)
    }

    Box(
        modifier = modifier
            .size(button.sizeDp.dp)
            .testTag("analog_stick_${button.id}")
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
                    Modifier.pointerInput(button.id) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val currentX = knobOffsetX.value + dragAmount.x
                                val currentY = knobOffsetY.value + dragAmount.y

                                val distance = sqrt(currentX * currentX + currentY * currentY)
                                val clampedDist = distance.coerceAtMost(maxRadiusPx)
                                val angle = atan2(currentY, currentX)

                                val finalX = clampedDist * cos(angle)
                                val finalY = clampedDist * sin(angle)

                                coroutineScope.launch {
                                    knobOffsetX.snapTo(finalX)
                                    knobOffsetY.snapTo(finalY)
                                }

                                val normX = finalX / maxRadiusPx
                                val normY = finalY / maxRadiusPx
                                onDirectionChanged(normX, normY)
                            },
                            onDragEnd = {
                                coroutineScope.launch {
                                    knobOffsetX.animateTo(0f, spring())
                                }
                                coroutineScope.launch {
                                    knobOffsetY.animateTo(0f, spring())
                                }
                                onDirectionChanged(0f, 0f)
                            },
                            onDragCancel = {
                                coroutineScope.launch {
                                    knobOffsetX.animateTo(0f, spring())
                                    knobOffsetY.animateTo(0f, spring())
                                }
                                onDirectionChanged(0f, 0f)
                            }
                        )
                    }
                }
            )
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF151D30).copy(alpha = button.opacity),
                        Color(0xFF0A0F1D).copy(alpha = button.opacity)
                    )
                )
            )
            .border(
                width = if (isSelected) 3.dp else 1.5.dp,
                color = if (isSelected) Color.White else accentColor.copy(alpha = 0.6f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        // Stick base guide lines
        Canvas(modifier = Modifier.size((button.sizeDp * 0.8f).dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            drawLine(
                color = accentColor.copy(alpha = 0.25f),
                start = Offset(center.x, 0f),
                end = Offset(center.x, size.height),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = accentColor.copy(alpha = 0.25f),
                start = Offset(0f, center.y),
                end = Offset(size.width, center.y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draggable thumb knob
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        knobOffsetX.value.roundToInt(),
                        knobOffsetY.value.roundToInt()
                    )
                }
                .size(knobSizeDp)
                .shadow(4.dp, CircleShape, ambientColor = accentColor, spotColor = accentColor)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.85f),
                            Color(0xFF1A2338)
                        )
                    )
                )
                .border(2.dp, accentColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (button.customLabel.isNotBlank()) button.customLabel else "L",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

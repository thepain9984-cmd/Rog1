package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HudButtonConfig
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import kotlin.math.roundToInt

@Composable
fun MappingReticleOverlay(
    buttons: List<HudButtonConfig>,
    screenWidthPx: Float,
    screenHeightPx: Float,
    selectedButtonId: String?,
    onTargetPositionChanged: (buttonId: String, deltaX: Float, deltaY: Float) -> Unit,
    onSelectButton: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Draw connecting laser lines between HUD button and target reticle
        Canvas(modifier = Modifier.fillMaxSize()) {
            buttons.forEach { button ->
                val isSelected = button.id == selectedButtonId
                val startX = button.hudXRatio * screenWidthPx
                val startY = button.hudYRatio * screenHeightPx
                val endX = button.targetXRatio * screenWidthPx
                val endY = button.targetYRatio * screenHeightPx

                val lineColor = if (isSelected) NeonPink else NeonCyan.copy(alpha = 0.5f)
                val strokeW = if (isSelected) 2.5.dp.toPx() else 1.2.dp.toPx()

                // Dashed laser line
                drawLine(
                    color = lineColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = strokeW,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
                )
            }
        }

        // Draggable Target Reticles
        buttons.forEach { button ->
            val isSelected = button.id == selectedButtonId
            val reticleX = (button.targetXRatio * screenWidthPx)
            val reticleY = (button.targetYRatio * screenHeightPx)

            val reticleColor = if (isSelected) NeonPink else NeonCyan

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (reticleX - 22.dp.toPx()).roundToInt(),
                            (reticleY - 22.dp.toPx()).roundToInt()
                        )
                    }
                    .size(44.dp)
                    .testTag("reticle_${button.id}")
                    .pointerInput(button.id) {
                        detectDragGestures(
                            onDragStart = { onSelectButton(button.id) },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onTargetPositionChanged(button.id, dragAmount.x, dragAmount.y)
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // Crosshair circle
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(reticleColor.copy(alpha = if (isSelected) 0.35f else 0.15f))
                        .border(
                            width = if (isSelected) 2.5.dp else 1.5.dp,
                            color = reticleColor,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Center dot
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(reticleColor)
                    )
                }

                // Crosshair ticks
                Canvas(modifier = Modifier.size(44.dp)) {
                    val c = Offset(size.width / 2, size.height / 2)
                    val r = 18.dp.toPx()
                    val tickLen = 6.dp.toPx()
                    // Top tick
                    drawLine(reticleColor, Offset(c.x, c.y - r - tickLen), Offset(c.x, c.y - r), 2.dp.toPx())
                    // Bottom tick
                    drawLine(reticleColor, Offset(c.x, c.y + r), Offset(c.x, c.y + r + tickLen), 2.dp.toPx())
                    // Left tick
                    drawLine(reticleColor, Offset(c.x - r - tickLen, c.y), Offset(c.x - r, c.y), 2.dp.toPx())
                    // Right tick
                    drawLine(reticleColor, Offset(c.x + r, c.y), Offset(c.x + r + tickLen, c.y), 2.dp.toPx())
                }

                // Small badge showing button label
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF0F172A).copy(alpha = 0.9f))
                        .border(0.8.dp, reticleColor.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "${button.name.take(6)} (${(button.targetXRatio * 100).roundToInt()}%, ${(button.targetYRatio * 100).roundToInt()}%)",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

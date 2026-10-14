package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.example.ui.components.AnalogStickView
import com.example.ui.components.HudButtonView
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

data class Bullet(
    var x: Float,
    var y: Float,
    val speed: Float = 25f
)

data class TargetOrb(
    val id: Long,
    var x: Float,
    var y: Float,
    var radius: Float = 22f,
    var hp: Int = 1,
    val color: Color
)

@Composable
fun PlayArenaScreen(
    profile: GameProfile,
    onButtonPress: (HudButtonConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    var shipXRatio by remember { mutableFloatStateOf(0.5f) }
    var shipYRatio by remember { mutableFloatStateOf(0.45f) }
    var score by remember { mutableIntStateOf(0) }
    var actionCount by remember { mutableIntStateOf(0) }
    var isShieldActive by remember { mutableStateOf(false) }
    var lastActionLabel by remember { mutableStateOf("READY") }

    val bullets = remember { mutableStateListOf<Bullet>() }
    val targets = remember { mutableStateListOf<TargetOrb>() }

    // Game loop for moving bullets and targets
    LaunchedEffect(Unit) {
        var lastSpawn = System.currentTimeMillis()
        while (true) {
            delay(16) // ~60 FPS

            // Update bullets
            val bulletsToRemove = mutableListOf<Bullet>()
            bullets.forEach { b ->
                b.y -= b.speed
                if (b.y < 0) bulletsToRemove.add(b)
            }
            bullets.removeAll(bulletsToRemove)

            // Spawn targets
            val now = System.currentTimeMillis()
            if (now - lastSpawn > 1400 && targets.size < 6) {
                lastSpawn = now
                val colors = listOf(NeonPink, NeonPurple, NeonYellow, NeonCyan)
                targets.add(
                    TargetOrb(
                        id = now,
                        x = Random.nextFloat().coerceIn(0.15f, 0.85f),
                        y = Random.nextFloat().coerceIn(0.12f, 0.40f),
                        color = colors.random()
                    )
                )
            }

            // Bullet - Target collision check
            val targetsToRemove = mutableListOf<TargetOrb>()
            val hitBullets = mutableListOf<Bullet>()

            bullets.forEach { b ->
                targets.forEach { t ->
                    val dist = kotlin.math.hypot(b.x - t.x, b.y - t.y)
                    if (dist < 40f) {
                        targetsToRemove.add(t)
                        hitBullets.add(b)
                        score += 100
                    }
                }
            }
            targets.removeAll(targetsToRemove)
            bullets.removeAll(hitBullets)
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .testTag("play_arena_screen")
    ) {
        val density = LocalDensity.current
        val screenWidthPx = with(density) { maxWidth.toPx() }
        val screenHeightPx = with(density) { maxHeight.toPx() }
        val laserOffsetX = with(density) { 12.dp.toPx() }
        val laserOffsetY = with(density) { 24.dp.toPx() }

        val shipX = shipXRatio * screenWidthPx
        val shipY = shipYRatio * screenHeightPx

        // Retro Space Game Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw background stars
            for (i in 0..30) {
                val seed = i * 47
                val sx = (seed * 19) % size.width
                val sy = (seed * 37) % size.height
                drawCircle(Color.White.copy(alpha = 0.25f), radius = 1.5.dp.toPx(), center = Offset(sx, sy))
            }

            // Draw Target Orbs
            targets.forEach { target ->
                val tx = target.x * size.width
                val ty = target.y * size.height
                // Glowing outer ring
                drawCircle(
                    color = target.color.copy(alpha = 0.3f),
                    radius = target.radius + 6.dp.toPx(),
                    center = Offset(tx, ty)
                )
                drawCircle(
                    color = target.color,
                    radius = target.radius,
                    center = Offset(tx, ty)
                )
            }

            // Draw Lasers / Bullets
            bullets.forEach { b ->
                drawLine(
                    color = NeonCyan,
                    start = Offset(b.x, b.y),
                    end = Offset(b.x, b.y + 16.dp.toPx()),
                    strokeWidth = 3.dp.toPx()
                )
            }

            // Draw Spaceship
            val shipPath = Path().apply {
                moveTo(shipX, shipY - 26.dp.toPx()) // Nose
                lineTo(shipX + 20.dp.toPx(), shipY + 20.dp.toPx()) // Right wing
                lineTo(shipX, shipY + 12.dp.toPx()) // Center engine
                lineTo(shipX - 20.dp.toPx(), shipY + 20.dp.toPx()) // Left wing
                close()
            }
            drawPath(shipPath, color = NeonCyan)

            // Draw Shield if active
            if (isShieldActive) {
                drawCircle(
                    color = NeonCyan.copy(alpha = 0.35f),
                    radius = 36.dp.toPx(),
                    center = Offset(shipX, shipY),
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }

        // Live Gaming Telemetry Top HUD
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, CyberCardBorder, RoundedCornerShape(16.dp))
                .align(Alignment.TopCenter),
            color = CyberSurface.copy(alpha = 0.88f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "SCORE", color = TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text(text = "$score", color = NeonCyan, fontSize = 18.sp, fontWeight = FontWeight.Black)
                }
                Column {
                    Text(text = "INPUT APM", color = TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text(text = "$actionCount", color = NeonGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text(text = "LAST KEY", color = TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text(text = lastActionLabel, color = NeonPink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                IconButton(
                    onClick = {
                        score = 0
                        actionCount = 0
                        targets.clear()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset Game", tint = TextSecondary, modifier = Modifier.size(18.dp))
                }
            }
        }

        // Virtual HUD Controls Overlay (Tied to the user's active profile!)
        profile.buttons.forEach { button ->
            val btnXPx = button.hudXRatio * screenWidthPx
            val btnYPx = button.hudYRatio * screenHeightPx
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
                        theme = profile.theme,
                        isEditMode = false,
                        onDirectionChanged = { dx, dy ->
                            onButtonPress(button)
                            actionCount++
                            lastActionLabel = "STICK MOVE"
                            // Move ship smoothly
                            shipXRatio = (shipXRatio + dx * 0.02f).coerceIn(0.1f, 0.9f)
                            shipYRatio = (shipYRatio + dy * 0.02f).coerceIn(0.1f, 0.6f)
                        }
                    )
                } else {
                    HudButtonView(
                        button = button,
                        theme = profile.theme,
                        isEditMode = false,
                        onPress = {
                            onButtonPress(button)
                            actionCount++
                            lastActionLabel = button.name.take(9)

                            // Interactive game actions based on button type
                            when (button.type) {
                                HudButtonType.ACTION_A, HudButtonType.TRIGGER_R2 -> {
                                    // Fire dual laser
                                    bullets.add(Bullet(x = shipX - laserOffsetX, y = shipY - laserOffsetY))
                                    bullets.add(Bullet(x = shipX + laserOffsetX, y = shipY - laserOffsetY))
                                }
                                HudButtonType.ACTION_B -> {
                                    // Toggle shield
                                    isShieldActive = !isShieldActive
                                }
                                HudButtonType.ACTION_Y, HudButtonType.BUMPER_R1, HudButtonType.SKILL_ULTIMATE -> {
                                    // Hyper Bomb blast!
                                    score += targets.size * 150
                                    targets.clear()
                                }
                                HudButtonType.DPAD_LEFT -> {
                                    shipXRatio = (shipXRatio - 0.05f).coerceAtLeast(0.1f)
                                }
                                HudButtonType.DPAD_RIGHT -> {
                                    shipXRatio = (shipXRatio + 0.05f).coerceAtMost(0.9f)
                                }
                                HudButtonType.DPAD_UP -> {
                                    shipYRatio = (shipYRatio - 0.05f).coerceAtLeast(0.1f)
                                }
                                HudButtonType.DPAD_DOWN -> {
                                    shipYRatio = (shipYRatio + 0.05f).coerceAtMost(0.6f)
                                }
                                else -> {
                                    // Normal action: fire laser
                                    bullets.add(Bullet(x = shipX, y = shipY - laserOffsetY))
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.ButtonShape
import com.example.model.HudButtonConfig
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun ButtonInspectorDialog(
    button: HudButtonConfig,
    onDismiss: () -> Unit,
    onSave: (HudButtonConfig) -> Unit,
    onDelete: (String) -> Unit
) {
    var label by remember { mutableStateOf(button.customLabel) }
    var sizeDp by remember { mutableFloatStateOf(button.sizeDp) }
    var opacity by remember { mutableFloatStateOf(button.opacity) }
    var isTurbo by remember { mutableStateOf(button.isTurbo) }
    var selectedShape by remember { mutableStateOf(button.shape) }
    var selectedColorHex by remember { mutableStateOf(button.customColorHex) }

    val presetColors = listOf(
        "", // default theme
        "#00E5FF", // Cyan
        "#EC4899", // Pink
        "#A855F7", // Purple
        "#10B981", // Green
        "#FBBF24", // Yellow
        "#EF4444", // Red
        "#3B82F6"  // Blue
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, CyberCardBorder, RoundedCornerShape(20.dp)),
            color = CyberSurface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Button Config",
                            color = NeonCyan,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = button.name,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("dialog_close_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Custom Label
                Text("Custom Button Label", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it.take(10) },
                    placeholder = { Text("Default", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = CyberCardBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("button_label_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Size Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Button Size", color = TextPrimary, fontSize = 13.sp)
                    Text("${sizeDp.roundToInt()} dp", color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = sizeDp,
                    onValueChange = { sizeDp = it },
                    valueRange = 36f..120f,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NeonCyan
                    ),
                    modifier = Modifier.testTag("button_size_slider")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Opacity Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Opacity", color = TextPrimary, fontSize = 13.sp)
                    Text("${(opacity * 100).roundToInt()}%", color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = opacity,
                    onValueChange = { opacity = it },
                    valueRange = 0.2f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NeonCyan
                    ),
                    modifier = Modifier.testTag("button_opacity_slider")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Turbo Fire Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberCard)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Turbo Auto-Fire", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("Rapid-fire repeat tap while button is held", color = TextSecondary, fontSize = 11.sp)
                    }
                    Switch(
                        checked = isTurbo,
                        onCheckedChange = { isTurbo = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = NeonPink
                        ),
                        modifier = Modifier.testTag("turbo_switch")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Shape selection
                Text("Button Shape", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ButtonShape.values().forEach { shape ->
                        val isSelected = selectedShape == shape
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else CyberCard)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) NeonCyan else CyberCardBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedShape = shape }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = shape.name.take(4),
                                color = if (isSelected) NeonCyan else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Accent Color
                Text("Color Accent", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetColors.forEach { hex ->
                        val isSelected = selectedColorHex == hex
                        val chipColor = if (hex.isBlank()) Color(0xFF64748B) else Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(chipColor)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorHex = hex }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onDelete(button.id) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonRed),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("delete_button_btn")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val updated = button.copy(
                                customLabel = label,
                                sizeDp = sizeDp,
                                opacity = opacity,
                                isTurbo = isTurbo,
                                shape = selectedShape,
                                customColorHex = selectedColorHex
                            )
                            onSave(updated)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan,
                            contentColor = Color(0xFF031622)
                        ),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("save_button_config_btn")
                    ) {
                        Text("Apply Changes", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

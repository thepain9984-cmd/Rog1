package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.TextButton
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
import com.example.model.GameProfile
import com.example.model.HapticStrength
import com.example.model.HudTheme
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun ProfilesScreen(
    profiles: List<GameProfile>,
    activeProfileId: String,
    onSelectProfile: (String) -> Unit,
    onSaveProfile: (GameProfile) -> Unit,
    onDuplicateProfile: (String) -> Unit,
    onDeleteProfile: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var profileToCustomize by remember { mutableStateOf<GameProfile?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var newProfileName by remember { mutableStateOf("") }
    var newProfileCategory by remember { mutableStateOf("Shooter") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(16.dp)
            .testTag("profiles_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Game Profiles",
                    color = NeonCyan,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Switch or customize layouts for your favorite games",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonCyan,
                    contentColor = Color(0xFF031622)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("create_profile_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Profile", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Profiles List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(profiles) { profile ->
                val isActive = profile.id == activeProfileId

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            width = if (isActive) 1.8.dp else 1.dp,
                            color = if (isActive) NeonCyan else CyberCardBorder,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onSelectProfile(profile.id) }
                        .testTag("profile_card_${profile.id}"),
                    colors = CardDefaults.cardColors(containerColor = CyberCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (isActive) NeonCyan.copy(alpha = 0.2f) else CyberSurface)
                                        .border(1.dp, if (isActive) NeonCyan else CyberCardBorder, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SportsEsports,
                                        contentDescription = "Game Icon",
                                        tint = if (isActive) NeonCyan else TextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = profile.name,
                                            color = TextPrimary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (isActive) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(NeonGreen.copy(alpha = 0.2f))
                                                    .border(0.8.dp, NeonGreen, RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("ACTIVE", color = NeonGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    Text(
                                        text = "${profile.gameCategory} • ${profile.buttons.size} Controls • ${profile.theme.displayName}",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            // Quick Actions
                            Row {
                                IconButton(
                                    onClick = { onDuplicateProfile(profile.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                }
                                if (!profile.isPreset) {
                                    IconButton(
                                        onClick = { onDeleteProfile(profile.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = NeonRed.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        if (profile.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = profile.description,
                                color = TextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Bottom Actions row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { profileToCustomize = profile },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan)
                            ) {
                                Text("Customize", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }

                            if (!isActive) {
                                Button(
                                    onClick = { onSelectProfile(profile.id) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = NeonCyan,
                                        contentColor = Color(0xFF031622)
                                    )
                                ) {
                                    Text("Activate", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Create Profile Dialog
        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("Create New Game Profile", color = NeonCyan, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Profile Name", color = TextPrimary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = newProfileName,
                            onValueChange = { newProfileName = it },
                            placeholder = { Text("e.g. Genshin Impact / Roblox", color = TextSecondary) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = CyberCardBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Category", color = TextPrimary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = newProfileCategory,
                            onValueChange = { newProfileCategory = it },
                            placeholder = { Text("e.g. Action RPG, Battle Royale", color = TextSecondary) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = CyberCardBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newProfileName.isNotBlank()) {
                                val newProfile = GameProfile(
                                    id = "profile_custom_${System.currentTimeMillis()}",
                                    name = newProfileName.trim(),
                                    description = "Custom gamer layout for $newProfileName",
                                    gameCategory = newProfileCategory.trim(),
                                    buttons = emptyList()
                                )
                                onSaveProfile(newProfile)
                                onSelectProfile(newProfile.id)
                                showCreateDialog = false
                                newProfileName = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color(0xFF031622))
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                containerColor = CyberSurface
            )
        }

        // Profile Customization Dialog
        if (profileToCustomize != null) {
            ProfileSettingsDialog(
                profile = profileToCustomize!!,
                onDismiss = { profileToCustomize = null },
                onSave = { updated ->
                    onSaveProfile(updated)
                    profileToCustomize = null
                }
            )
        }
    }
}

@Composable
fun ProfileSettingsDialog(
    profile: GameProfile,
    onDismiss: () -> Unit,
    onSave: (GameProfile) -> Unit
) {
    var theme by remember { mutableStateOf(profile.theme) }
    var haptic by remember { mutableStateOf(profile.hapticStrength) }
    var soundEnabled by remember { mutableStateOf(profile.soundEnabled) }
    var opacity by remember { mutableFloatStateOf(profile.globalOpacity) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Customize ${profile.name}",
                color = NeonCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Column {
                // Theme Selection
                Text("Controller Skin / Theme", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                HudTheme.values().forEach { t ->
                    val isSelected = theme == t
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) NeonCyan.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable { theme = t }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(t.displayName, color = if (isSelected) NeonCyan else TextPrimary, fontSize = 13.sp)
                        if (isSelected) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Haptic Feedback
                Text("Tactile Haptic Rumble", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HapticStrength.values().forEach { h ->
                        val isSelected = haptic == h
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) NeonPurple.copy(alpha = 0.25f) else CyberCard)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) NeonPurple else CyberCardBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { haptic = h }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(h.displayName, color = if (isSelected) NeonPurple else TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Sound Effects Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Sound Effects (SFX)", color = TextPrimary, fontSize = 13.sp)
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = { soundEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = NeonCyan
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Global HUD Opacity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("HUD Opacity", color = TextPrimary, fontSize = 13.sp)
                    Text("${(opacity * 100).roundToInt()}%", color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = opacity,
                    onValueChange = { opacity = it },
                    valueRange = 0.2f..1.0f,
                    colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        profile.copy(
                            theme = theme,
                            hapticStrength = haptic,
                            soundEnabled = soundEnabled,
                            globalOpacity = opacity
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color(0xFF031622))
            ) {
                Text("Save Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = CyberSurface
    )
}

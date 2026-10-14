package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.HudStudioScreen
import com.example.ui.screens.OverlaySetupScreen
import com.example.ui.screens.PlayArenaScreen
import com.example.ui.screens.ProfilesScreen
import com.example.ui.theme.ConsoleHudTheme
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MainViewModel

enum class NavScreen(val label: String) {
    STUDIO("HUD Studio"),
    ARENA("Play Arena"),
    PROFILES("Profiles"),
    OVERLAY("Overlay")
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ConsoleHudTheme {
                var currentScreen by remember { mutableStateOf(NavScreen.STUDIO) }

                BackHandler(enabled = currentScreen != NavScreen.STUDIO) {
                    currentScreen = NavScreen.STUDIO
                }

                val activeProfile by viewModel.activeProfile.collectAsState()
                val profiles by viewModel.profiles.collectAsState()
                val isFloatingRunning by viewModel.isFloatingServiceRunning.collectAsState()

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CyberBackground),
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier
                                .border(0.8.dp, CyberCardBorder)
                                .navigationBarsPadding()
                                .testTag("main_navigation_bar"),
                            containerColor = CyberSurface,
                            tonalElevation = 8.dp
                        ) {
                            NavigationBarItem(
                                selected = currentScreen == NavScreen.STUDIO,
                                onClick = { currentScreen = NavScreen.STUDIO },
                                icon = {
                                    Icon(
                                        Icons.Default.Tune,
                                        contentDescription = "HUD Studio",
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                label = { Text(NavScreen.STUDIO.label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = NeonCyan,
                                    selectedTextColor = NeonCyan,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary,
                                    indicatorColor = NeonCyan.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier.testTag("nav_item_studio")
                            )

                            NavigationBarItem(
                                selected = currentScreen == NavScreen.ARENA,
                                onClick = { currentScreen = NavScreen.ARENA },
                                icon = {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = "Play Arena",
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                label = { Text(NavScreen.ARENA.label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = NeonCyan,
                                    selectedTextColor = NeonCyan,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary,
                                    indicatorColor = NeonCyan.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier.testTag("nav_item_arena")
                            )

                            NavigationBarItem(
                                selected = currentScreen == NavScreen.PROFILES,
                                onClick = { currentScreen = NavScreen.PROFILES },
                                icon = {
                                    Icon(
                                        Icons.Default.Gamepad,
                                        contentDescription = "Profiles",
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                label = { Text(NavScreen.PROFILES.label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = NeonCyan,
                                    selectedTextColor = NeonCyan,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary,
                                    indicatorColor = NeonCyan.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier.testTag("nav_item_profiles")
                            )

                            NavigationBarItem(
                                selected = currentScreen == NavScreen.OVERLAY,
                                onClick = { currentScreen = NavScreen.OVERLAY },
                                icon = {
                                    Icon(
                                        Icons.Default.Layers,
                                        contentDescription = "Overlay Center",
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                label = { Text(NavScreen.OVERLAY.label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = NeonCyan,
                                    selectedTextColor = NeonCyan,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary,
                                    indicatorColor = NeonCyan.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier.testTag("nav_item_overlay")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .statusBarsPadding()
                    ) {
                        when (currentScreen) {
                            NavScreen.STUDIO -> {
                                HudStudioScreen(
                                    profile = activeProfile,
                                    allProfiles = profiles,
                                    onSelectProfile = { viewModel.selectProfile(it) },
                                    onUpdateButton = { viewModel.updateButton(it) },
                                    onAddButton = { viewModel.addButton(it) },
                                    onDeleteButton = { viewModel.deleteButton(it) },
                                    onResetDefaults = { viewModel.resetDefaults() },
                                    onButtonPress = { viewModel.triggerButtonPress(it) }
                                )
                            }
                            NavScreen.ARENA -> {
                                PlayArenaScreen(
                                    profile = activeProfile,
                                    onButtonPress = { viewModel.triggerButtonPress(it) }
                                )
                            }
                            NavScreen.PROFILES -> {
                                ProfilesScreen(
                                    profiles = profiles,
                                    activeProfileId = activeProfile.id,
                                    onSelectProfile = { viewModel.selectProfile(it) },
                                    onSaveProfile = { viewModel.saveProfile(it) },
                                    onDuplicateProfile = { viewModel.duplicateProfile(it) },
                                    onDeleteProfile = { viewModel.deleteProfile(it) }
                                )
                            }
                            NavScreen.OVERLAY -> {
                                OverlaySetupScreen(
                                    activeProfile = activeProfile,
                                    isFloatingRunning = isFloatingRunning,
                                    onToggleFloatingService = { viewModel.toggleFloatingService() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ProfileRepository
import com.example.model.GameProfile
import com.example.model.HudButtonConfig
import com.example.service.FloatingHudService
import com.example.service.TouchAccessibilityService
import com.example.util.HapticManager
import com.example.util.SoundManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProfileRepository(application)
    private val hapticManager = HapticManager(application)
    private val soundManager = SoundManager()

    val profiles: StateFlow<List<GameProfile>> = repository.profiles

    val activeProfile: StateFlow<GameProfile> = combine(
        repository.profiles,
        repository.activeProfileId
    ) { profs, activeId ->
        profs.find { it.id == activeId } ?: profs.firstOrNull() ?: repository.getActiveProfile()
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        repository.getActiveProfile()
    )

    val isFloatingServiceRunning: StateFlow<Boolean> = FloatingHudService.isFloatingHudRunning

    fun selectProfile(profileId: String) {
        repository.setActiveProfile(profileId)
    }

    fun updateButton(button: HudButtonConfig) {
        val active = activeProfile.value
        repository.updateButtonConfig(active.id, button)
    }

    fun addButton(button: HudButtonConfig) {
        val active = activeProfile.value
        repository.addButton(active.id, button)
    }

    fun deleteButton(buttonId: String) {
        val active = activeProfile.value
        repository.removeButton(active.id, buttonId)
    }

    fun duplicateProfile(profileId: String) {
        repository.duplicateProfile(profileId)
    }

    fun deleteProfile(profileId: String) {
        repository.deleteProfile(profileId)
    }

    fun saveProfile(profile: GameProfile) {
        repository.saveProfile(profile)
    }

    fun resetDefaults() {
        repository.resetToDefaults()
    }

    fun triggerButtonPress(button: HudButtonConfig) {
        val profile = activeProfile.value
        // Haptics
        hapticManager.vibrate(profile.hapticStrength)
        // SFX
        if (profile.soundEnabled) {
            if (button.isTurbo) soundManager.playTurboSound() else soundManager.playClickSound()
        }

        // Touch gesture injection if Accessibility is active
        if (TouchAccessibilityService.isConnected()) {
            val dm = getApplication<Application>().resources.displayMetrics
            val px = button.targetXRatio * dm.widthPixels
            val py = button.targetYRatio * dm.heightPixels
            TouchAccessibilityService.tap(px, py, 40L)
        }
    }

    fun toggleFloatingService() {
        val context = getApplication<Application>()
        if (isFloatingServiceRunning.value) {
            FloatingHudService.stop(context)
        } else {
            FloatingHudService.start(context)
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }
}

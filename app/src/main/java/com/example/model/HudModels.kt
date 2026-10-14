package com.example.model

enum class HudButtonType {
    // Action diamond buttons
    ACTION_A,
    ACTION_B,
    ACTION_X,
    ACTION_Y,

    // Directional controls
    DPAD_UP,
    DPAD_DOWN,
    DPAD_LEFT,
    DPAD_RIGHT,
    ANALOG_STICK, // Combined 360 joystick
    ANALOG_STICK_RIGHT, // Aim / Look stick

    // Shoulders & Triggers
    BUMPER_L1,
    BUMPER_R1,
    TRIGGER_L2,
    TRIGGER_R2,

    // System & thumb clicks
    BUTTON_START,
    BUTTON_SELECT,
    THUMB_L3,
    THUMB_R3,

    // Mobile game special actions (Reload, Sprint, Skill 1, 2, Ult, Heal)
    SKILL_1,
    SKILL_2,
    SKILL_ULTIMATE,
    ACTION_RELOAD,
    ACTION_JUMP,
    ACTION_HEAL
}

enum class HudTheme(val displayName: String) {
    CYBERPUNK("Cyberpunk Neon"),
    PLAYSTATION("PlayStation Classic"),
    XBOX("Xbox Carbon"),
    SWITCH("Switch Joy-Con"),
    STEALTH("Stealth Minimalist")
}

enum class HapticStrength(val displayName: String, val durationMs: Long) {
    OFF("Off", 0L),
    LIGHT("Light", 18L),
    MEDIUM("Medium", 35L),
    STRONG("Heavy", 65L)
}

enum class ButtonShape {
    CIRCLE,
    ROUNDED_SQUARE,
    PILL,
    OCTAGON
}

data class HudButtonConfig(
    val id: String,
    val name: String,
    val type: HudButtonType,
    // Position on screen where the HUD button sits (0.0f .. 1.0f relative to screen width/height)
    val hudXRatio: Float,
    val hudYRatio: Float,
    // Target position on screen where the touch tap/swipe is simulated (0.0f .. 1.0f)
    val targetXRatio: Float,
    val targetYRatio: Float,
    val sizeDp: Float = 64f,
    val opacity: Float = 0.85f,
    val customLabel: String = "",
    val customColorHex: String = "",
    val shape: ButtonShape = ButtonShape.CIRCLE,
    val isTurbo: Boolean = false,
    val isEnabled: Boolean = true,
    val isHoldToRepeat: Boolean = false
)

data class GameProfile(
    val id: String,
    val name: String,
    val description: String,
    val gameCategory: String,
    val theme: HudTheme = HudTheme.CYBERPUNK,
    val hapticStrength: HapticStrength = HapticStrength.MEDIUM,
    val soundEnabled: Boolean = true,
    val globalOpacity: Float = 0.85f,
    val globalScale: Float = 1.0f,
    val buttons: List<HudButtonConfig> = emptyList(),
    val isPreset: Boolean = false
)

data class TouchTelemetry(
    val buttonId: String,
    val buttonName: String,
    val screenX: Float,
    val screenY: Float,
    val timestamp: Long = System.currentTimeMillis(),
    val isDown: Boolean = true
)

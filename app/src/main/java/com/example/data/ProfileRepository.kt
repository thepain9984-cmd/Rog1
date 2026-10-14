package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.ButtonShape
import com.example.model.GameProfile
import com.example.model.HapticStrength
import com.example.model.HudButtonConfig
import com.example.model.HudButtonType
import com.example.model.HudTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class ProfileRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("console_hud_profiles", Context.MODE_PRIVATE)

    private val _profiles = MutableStateFlow<List<GameProfile>>(emptyList())
    val profiles: StateFlow<List<GameProfile>> = _profiles.asStateFlow()

    private val _activeProfileId = MutableStateFlow<String>("")
    val activeProfileId: StateFlow<String> = _activeProfileId.asStateFlow()

    init {
        loadProfiles()
    }

    fun getActiveProfile(): GameProfile {
        val currentId = _activeProfileId.value
        return _profiles.value.find { it.id == currentId }
            ?: _profiles.value.firstOrNull()
            ?: createDefaultPlatformerProfile()
    }

    fun setActiveProfile(profileId: String) {
        if (_profiles.value.any { it.id == profileId }) {
            _activeProfileId.value = profileId
            prefs.edit().putString(KEY_ACTIVE_PROFILE_ID, profileId).apply()
        }
    }

    fun saveProfile(profile: GameProfile) {
        val currentList = _profiles.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == profile.id }
        if (index >= 0) {
            currentList[index] = profile
        } else {
            currentList.add(profile)
        }
        _profiles.value = currentList
        persistProfiles(currentList)
    }

    fun updateButtonConfig(profileId: String, updatedButton: HudButtonConfig) {
        val profile = _profiles.value.find { it.id == profileId } ?: return
        val updatedButtons = profile.buttons.map {
            if (it.id == updatedButton.id) updatedButton else it
        }
        saveProfile(profile.copy(buttons = updatedButtons))
    }

    fun addButton(profileId: String, button: HudButtonConfig) {
        val profile = _profiles.value.find { it.id == profileId } ?: return
        saveProfile(profile.copy(buttons = profile.buttons + button))
    }

    fun removeButton(profileId: String, buttonId: String) {
        val profile = _profiles.value.find { it.id == profileId } ?: return
        saveProfile(profile.copy(buttons = profile.buttons.filter { it.id != buttonId }))
    }

    fun duplicateProfile(profileId: String, newName: String? = null): GameProfile? {
        val original = _profiles.value.find { it.id == profileId } ?: return null
        val newId = "profile_" + System.currentTimeMillis()
        val duplicated = original.copy(
            id = newId,
            name = newName ?: "${original.name} (Copy)",
            isPreset = false
        )
        saveProfile(duplicated)
        setActiveProfile(newId)
        return duplicated
    }

    fun deleteProfile(profileId: String): Boolean {
        val currentList = _profiles.value.toMutableList()
        val toRemove = currentList.find { it.id == profileId } ?: return false
        if (toRemove.isPreset && currentList.count { it.isPreset } <= 1) {
            return false // Keep at least one default
        }
        currentList.remove(toRemove)
        _profiles.value = currentList
        persistProfiles(currentList)

        if (_activeProfileId.value == profileId) {
            val fallback = currentList.firstOrNull()?.id ?: createDefaultPlatformerProfile().id
            setActiveProfile(fallback)
        }
        return true
    }

    fun resetToDefaults() {
        val defaultProfiles = listOf(
            createDefaultPlatformerProfile(),
            createBattleRoyaleProfile(),
            createMobaRpgProfile(),
            createRacingProfile()
        )
        _profiles.value = defaultProfiles
        _activeProfileId.value = defaultProfiles.first().id
        persistProfiles(defaultProfiles)
        prefs.edit().putString(KEY_ACTIVE_PROFILE_ID, defaultProfiles.first().id).apply()
    }

    private fun loadProfiles() {
        val savedJson = prefs.getString(KEY_PROFILES_JSON, null)
        val list = if (savedJson.isNullOrBlank()) {
            listOf(
                createDefaultPlatformerProfile(),
                createBattleRoyaleProfile(),
                createMobaRpgProfile(),
                createRacingProfile()
            )
        } else {
            deserializeProfiles(savedJson)
        }

        _profiles.value = list
        val savedActive = prefs.getString(KEY_ACTIVE_PROFILE_ID, null)
        val active = if (savedActive != null && list.any { it.id == savedActive }) {
            savedActive
        } else {
            list.firstOrNull()?.id ?: ""
        }
        _activeProfileId.value = active
    }

    private fun persistProfiles(profiles: List<GameProfile>) {
        val jsonArray = JSONArray()
        profiles.forEach { p ->
            val pObj = JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("description", p.description)
                put("gameCategory", p.gameCategory)
                put("theme", p.theme.name)
                put("hapticStrength", p.hapticStrength.name)
                put("soundEnabled", p.soundEnabled)
                put("globalOpacity", p.globalOpacity.toDouble())
                put("globalScale", p.globalScale.toDouble())
                put("isPreset", p.isPreset)

                val btnArray = JSONArray()
                p.buttons.forEach { b ->
                    val bObj = JSONObject().apply {
                        put("id", b.id)
                        put("name", b.name)
                        put("type", b.type.name)
                        put("hudXRatio", b.hudXRatio.toDouble())
                        put("hudYRatio", b.hudYRatio.toDouble())
                        put("targetXRatio", b.targetXRatio.toDouble())
                        put("targetYRatio", b.targetYRatio.toDouble())
                        put("sizeDp", b.sizeDp.toDouble())
                        put("opacity", b.opacity.toDouble())
                        put("customLabel", b.customLabel)
                        put("customColorHex", b.customColorHex)
                        put("shape", b.shape.name)
                        put("isTurbo", b.isTurbo)
                        put("isEnabled", b.isEnabled)
                        put("isHoldToRepeat", b.isHoldToRepeat)
                    }
                    btnArray.put(bObj)
                }
                put("buttons", btnArray)
            }
            jsonArray.put(pObj)
        }
        prefs.edit().putString(KEY_PROFILES_JSON, jsonArray.toString()).apply()
    }

    private fun deserializeProfiles(jsonStr: String): List<GameProfile> {
        return try {
            val array = JSONArray(jsonStr)
            val result = mutableListOf<GameProfile>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.getString("id")
                val name = obj.getString("name")
                val desc = obj.optString("description", "")
                val category = obj.optString("gameCategory", "Custom")
                val theme = try {
                    HudTheme.valueOf(obj.optString("theme", HudTheme.CYBERPUNK.name))
                } catch (e: Exception) {
                    HudTheme.CYBERPUNK
                }
                val haptic = try {
                    HapticStrength.valueOf(obj.optString("hapticStrength", HapticStrength.MEDIUM.name))
                } catch (e: Exception) {
                    HapticStrength.MEDIUM
                }
                val soundEnabled = obj.optBoolean("soundEnabled", true)
                val opacity = obj.optDouble("globalOpacity", 0.85).toFloat()
                val scale = obj.optDouble("globalScale", 1.0).toFloat()
                val isPreset = obj.optBoolean("isPreset", false)

                val btnArray = obj.optJSONArray("buttons") ?: JSONArray()
                val buttons = mutableListOf<HudButtonConfig>()
                for (j in 0 until btnArray.length()) {
                    val bObj = btnArray.getJSONObject(j)
                    val bType = try {
                        HudButtonType.valueOf(bObj.getString("type"))
                    } catch (e: Exception) {
                        HudButtonType.ACTION_A
                    }
                    val shape = try {
                        ButtonShape.valueOf(bObj.optString("shape", ButtonShape.CIRCLE.name))
                    } catch (e: Exception) {
                        ButtonShape.CIRCLE
                    }

                    buttons.add(
                        HudButtonConfig(
                            id = bObj.getString("id"),
                            name = bObj.getString("name"),
                            type = bType,
                            hudXRatio = bObj.getDouble("hudXRatio").toFloat(),
                            hudYRatio = bObj.getDouble("hudYRatio").toFloat(),
                            targetXRatio = bObj.getDouble("targetXRatio").toFloat(),
                            targetYRatio = bObj.getDouble("targetYRatio").toFloat(),
                            sizeDp = bObj.optDouble("sizeDp", 64.0).toFloat(),
                            opacity = bObj.optDouble("opacity", 0.85).toFloat(),
                            customLabel = bObj.optString("customLabel", ""),
                            customColorHex = bObj.optString("customColorHex", ""),
                            shape = shape,
                            isTurbo = bObj.optBoolean("isTurbo", false),
                            isEnabled = bObj.optBoolean("isEnabled", true),
                            isHoldToRepeat = bObj.optBoolean("isHoldToRepeat", false)
                        )
                    )
                }

                result.add(
                    GameProfile(
                        id = id,
                        name = name,
                        description = desc,
                        gameCategory = category,
                        theme = theme,
                        hapticStrength = haptic,
                        soundEnabled = soundEnabled,
                        globalOpacity = opacity,
                        globalScale = scale,
                        buttons = buttons,
                        isPreset = isPreset
                    )
                )
            }
            if (result.isEmpty()) listOf(createDefaultPlatformerProfile()) else result
        } catch (e: Exception) {
            listOf(createDefaultPlatformerProfile())
        }
    }

    private fun createDefaultPlatformerProfile(): GameProfile {
        return GameProfile(
            id = "preset_retro_platformer",
            name = "Retro Console (D-Pad + ABXY)",
            description = "Classic arcade layout for platformers, fighting, and retro emulator games.",
            gameCategory = "Platformer / Arcade",
            theme = HudTheme.CYBERPUNK,
            hapticStrength = HapticStrength.MEDIUM,
            soundEnabled = true,
            isPreset = true,
            buttons = listOf(
                HudButtonConfig(
                    id = "dpad_up",
                    name = "D-Pad Up",
                    type = HudButtonType.DPAD_UP,
                    hudXRatio = 0.18f,
                    hudYRatio = 0.65f,
                    targetXRatio = 0.18f,
                    targetYRatio = 0.65f,
                    sizeDp = 52f,
                    shape = ButtonShape.ROUNDED_SQUARE
                ),
                HudButtonConfig(
                    id = "dpad_down",
                    name = "D-Pad Down",
                    type = HudButtonType.DPAD_DOWN,
                    hudXRatio = 0.18f,
                    hudYRatio = 0.81f,
                    targetXRatio = 0.18f,
                    targetYRatio = 0.81f,
                    sizeDp = 52f,
                    shape = ButtonShape.ROUNDED_SQUARE
                ),
                HudButtonConfig(
                    id = "dpad_left",
                    name = "D-Pad Left",
                    type = HudButtonType.DPAD_LEFT,
                    hudXRatio = 0.10f,
                    hudYRatio = 0.73f,
                    targetXRatio = 0.10f,
                    targetYRatio = 0.73f,
                    sizeDp = 52f,
                    shape = ButtonShape.ROUNDED_SQUARE
                ),
                HudButtonConfig(
                    id = "dpad_right",
                    name = "D-Pad Right",
                    type = HudButtonType.DPAD_RIGHT,
                    hudXRatio = 0.26f,
                    hudYRatio = 0.73f,
                    targetXRatio = 0.26f,
                    targetYRatio = 0.73f,
                    sizeDp = 52f,
                    shape = ButtonShape.ROUNDED_SQUARE
                ),
                // Action Buttons Diamond
                HudButtonConfig(
                    id = "btn_y",
                    name = "Action Y (North)",
                    type = HudButtonType.ACTION_Y,
                    hudXRatio = 0.82f,
                    hudYRatio = 0.65f,
                    targetXRatio = 0.82f,
                    targetYRatio = 0.65f,
                    sizeDp = 56f
                ),
                HudButtonConfig(
                    id = "btn_a",
                    name = "Action A (Jump/South)",
                    type = HudButtonType.ACTION_A,
                    hudXRatio = 0.82f,
                    hudYRatio = 0.81f,
                    targetXRatio = 0.82f,
                    targetYRatio = 0.81f,
                    sizeDp = 56f
                ),
                HudButtonConfig(
                    id = "btn_x",
                    name = "Action X (Attack/West)",
                    type = HudButtonType.ACTION_X,
                    hudXRatio = 0.73f,
                    hudYRatio = 0.73f,
                    targetXRatio = 0.73f,
                    targetYRatio = 0.73f,
                    sizeDp = 56f
                ),
                HudButtonConfig(
                    id = "btn_b",
                    name = "Action B (Dash/East)",
                    type = HudButtonType.ACTION_B,
                    hudXRatio = 0.91f,
                    hudYRatio = 0.73f,
                    targetXRatio = 0.91f,
                    targetYRatio = 0.73f,
                    sizeDp = 56f
                ),
                // Bumpers
                HudButtonConfig(
                    id = "btn_l1",
                    name = "Bumper L1",
                    type = HudButtonType.BUMPER_L1,
                    hudXRatio = 0.14f,
                    hudYRatio = 0.24f,
                    targetXRatio = 0.14f,
                    targetYRatio = 0.24f,
                    sizeDp = 54f,
                    shape = ButtonShape.PILL
                ),
                HudButtonConfig(
                    id = "btn_r1",
                    name = "Bumper R1 (Turbo)",
                    type = HudButtonType.BUMPER_R1,
                    hudXRatio = 0.86f,
                    hudYRatio = 0.24f,
                    targetXRatio = 0.86f,
                    targetYRatio = 0.24f,
                    sizeDp = 54f,
                    shape = ButtonShape.PILL,
                    isTurbo = true
                ),
                // Start / Select
                HudButtonConfig(
                    id = "btn_select",
                    name = "Select",
                    type = HudButtonType.BUTTON_SELECT,
                    hudXRatio = 0.42f,
                    hudYRatio = 0.88f,
                    targetXRatio = 0.42f,
                    targetYRatio = 0.88f,
                    sizeDp = 44f,
                    shape = ButtonShape.PILL
                ),
                HudButtonConfig(
                    id = "btn_start",
                    name = "Start / Pause",
                    type = HudButtonType.BUTTON_START,
                    hudXRatio = 0.58f,
                    hudYRatio = 0.88f,
                    targetXRatio = 0.58f,
                    targetYRatio = 0.88f,
                    sizeDp = 44f,
                    shape = ButtonShape.PILL
                )
            )
        )
    }

    private fun createBattleRoyaleProfile(): GameProfile {
        return GameProfile(
            id = "preset_battle_royale",
            name = "Battle Royale & Shooter (FPS)",
            description = "Optimized for PUBG, COD, Free Fire with Analog Move, Trigger Fire, Jump, Aim, and Reload.",
            gameCategory = "Shooter / FPS",
            theme = HudTheme.XBOX,
            hapticStrength = HapticStrength.STRONG,
            soundEnabled = true,
            isPreset = true,
            buttons = listOf(
                HudButtonConfig(
                    id = "stick_move",
                    name = "Analog Movement Stick",
                    type = HudButtonType.ANALOG_STICK,
                    hudXRatio = 0.18f,
                    hudYRatio = 0.72f,
                    targetXRatio = 0.18f,
                    targetYRatio = 0.72f,
                    sizeDp = 100f
                ),
                HudButtonConfig(
                    id = "btn_fire",
                    name = "Shoot / Fire (R2)",
                    type = HudButtonType.TRIGGER_R2,
                    hudXRatio = 0.84f,
                    hudYRatio = 0.62f,
                    targetXRatio = 0.84f,
                    targetYRatio = 0.62f,
                    sizeDp = 68f,
                    isTurbo = true
                ),
                HudButtonConfig(
                    id = "btn_aim",
                    name = "Aim / Scope (L2)",
                    type = HudButtonType.TRIGGER_L2,
                    hudXRatio = 0.16f,
                    hudYRatio = 0.38f,
                    targetXRatio = 0.16f,
                    targetYRatio = 0.38f,
                    sizeDp = 58f
                ),
                HudButtonConfig(
                    id = "btn_jump",
                    name = "Jump",
                    type = HudButtonType.ACTION_JUMP,
                    hudXRatio = 0.88f,
                    hudYRatio = 0.76f,
                    targetXRatio = 0.88f,
                    targetYRatio = 0.76f,
                    sizeDp = 56f
                ),
                HudButtonConfig(
                    id = "btn_reload",
                    name = "Reload Ammo",
                    type = HudButtonType.ACTION_RELOAD,
                    hudXRatio = 0.86f,
                    hudYRatio = 0.48f,
                    targetXRatio = 0.86f,
                    targetYRatio = 0.48f,
                    sizeDp = 52f
                ),
                HudButtonConfig(
                    id = "btn_crouch",
                    name = "Crouch / Slide",
                    type = HudButtonType.ACTION_B,
                    hudXRatio = 0.75f,
                    hudYRatio = 0.82f,
                    targetXRatio = 0.75f,
                    targetYRatio = 0.82f,
                    sizeDp = 52f
                )
            )
        )
    }

    private fun createMobaRpgProfile(): GameProfile {
        return GameProfile(
            id = "preset_moba_rpg",
            name = "MOBA & Action RPG (Genshin / Wild Rift)",
            description = "Dual-stick style with main attack, 3 skill buttons, ultimate, and quick heal.",
            gameCategory = "MOBA / Action RPG",
            theme = HudTheme.PLAYSTATION,
            hapticStrength = HapticStrength.MEDIUM,
            soundEnabled = true,
            isPreset = true,
            buttons = listOf(
                HudButtonConfig(
                    id = "stick_moba",
                    name = "Movement Joystick",
                    type = HudButtonType.ANALOG_STICK,
                    hudXRatio = 0.18f,
                    hudYRatio = 0.72f,
                    targetXRatio = 0.18f,
                    targetYRatio = 0.72f,
                    sizeDp = 100f
                ),
                HudButtonConfig(
                    id = "btn_normal_atk",
                    name = "Basic Attack",
                    type = HudButtonType.ACTION_A,
                    hudXRatio = 0.84f,
                    hudYRatio = 0.74f,
                    targetXRatio = 0.84f,
                    targetYRatio = 0.74f,
                    sizeDp = 70f
                ),
                HudButtonConfig(
                    id = "btn_skill1",
                    name = "Skill 1",
                    type = HudButtonType.SKILL_1,
                    hudXRatio = 0.72f,
                    hudYRatio = 0.82f,
                    targetXRatio = 0.72f,
                    targetYRatio = 0.82f,
                    sizeDp = 54f
                ),
                HudButtonConfig(
                    id = "btn_skill2",
                    name = "Skill 2",
                    type = HudButtonType.SKILL_2,
                    hudXRatio = 0.74f,
                    hudYRatio = 0.64f,
                    targetXRatio = 0.74f,
                    targetYRatio = 0.64f,
                    sizeDp = 54f
                ),
                HudButtonConfig(
                    id = "btn_ult",
                    name = "Ultimate",
                    type = HudButtonType.SKILL_ULTIMATE,
                    hudXRatio = 0.86f,
                    hudYRatio = 0.54f,
                    targetXRatio = 0.86f,
                    targetYRatio = 0.54f,
                    sizeDp = 58f
                ),
                HudButtonConfig(
                    id = "btn_heal",
                    name = "Heal / Potion",
                    type = HudButtonType.ACTION_HEAL,
                    hudXRatio = 0.62f,
                    hudYRatio = 0.85f,
                    targetXRatio = 0.62f,
                    targetYRatio = 0.85f,
                    sizeDp = 48f
                )
            )
        )
    }

    private fun createRacingProfile(): GameProfile {
        return GameProfile(
            id = "preset_racing",
            name = "Arcade Speed Racing",
            description = "Left/Right steering pedals, Nitro boost, Gas pedal, and E-Brake drift.",
            gameCategory = "Racing",
            theme = HudTheme.SWITCH,
            hapticStrength = HapticStrength.STRONG,
            soundEnabled = true,
            isPreset = true,
            buttons = listOf(
                HudButtonConfig(
                    id = "steer_left",
                    name = "Steer Left",
                    type = HudButtonType.DPAD_LEFT,
                    hudXRatio = 0.12f,
                    hudYRatio = 0.75f,
                    targetXRatio = 0.12f,
                    targetYRatio = 0.75f,
                    sizeDp = 65f,
                    shape = ButtonShape.ROUNDED_SQUARE
                ),
                HudButtonConfig(
                    id = "steer_right",
                    name = "Steer Right",
                    type = HudButtonType.DPAD_RIGHT,
                    hudXRatio = 0.28f,
                    hudYRatio = 0.75f,
                    targetXRatio = 0.28f,
                    targetYRatio = 0.75f,
                    sizeDp = 65f,
                    shape = ButtonShape.ROUNDED_SQUARE
                ),
                HudButtonConfig(
                    id = "gas_pedal",
                    name = "Gas / Accelerate",
                    type = HudButtonType.TRIGGER_R2,
                    hudXRatio = 0.88f,
                    hudYRatio = 0.65f,
                    targetXRatio = 0.88f,
                    targetYRatio = 0.65f,
                    sizeDp = 70f
                ),
                HudButtonConfig(
                    id = "nitro_boost",
                    name = "Nitro / Turbo",
                    type = HudButtonType.ACTION_Y,
                    hudXRatio = 0.86f,
                    hudYRatio = 0.45f,
                    targetXRatio = 0.86f,
                    targetYRatio = 0.45f,
                    sizeDp = 56f
                ),
                HudButtonConfig(
                    id = "brake_drift",
                    name = "Brake / Drift",
                    type = HudButtonType.TRIGGER_L2,
                    hudXRatio = 0.74f,
                    hudYRatio = 0.80f,
                    targetXRatio = 0.74f,
                    targetYRatio = 0.80f,
                    sizeDp = 62f
                )
            )
        )
    }

    companion object {
        private const val KEY_PROFILES_JSON = "profiles_json"
        private const val KEY_ACTIVE_PROFILE_ID = "active_profile_id"
    }
}

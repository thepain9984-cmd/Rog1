package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.data.ProfileRepository
import com.example.model.GameProfile
import com.example.model.HudButtonConfig
import com.example.ui.components.FloatingHudOverlayView
import com.example.util.HapticManager
import com.example.util.SoundManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FloatingHudService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private lateinit var windowManager: WindowManager
    private var overlayComposeView: ComposeView? = null
    private var isOverlayAttached = false

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val appViewModelStore = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private lateinit var repository: ProfileRepository
    private lateinit var hapticManager: HapticManager
    private lateinit var soundManager: SoundManager

    private var activeProfile = MutableStateFlow<GameProfile?>(null)
    private var isEditMode = MutableStateFlow(false)
    private var isHudVisible = MutableStateFlow(true)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = appViewModelStore
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        repository = ProfileRepository(applicationContext)
        hapticManager = HapticManager(applicationContext)
        soundManager = SoundManager()

        _isFloatingHudRunning.value = true
        startForegroundNotification()
        setupOverlayView()

        serviceScope.launch {
            repository.profiles.collect {
                activeProfile.value = repository.getActiveProfile()
            }
        }
    }

    private fun startForegroundNotification() {
        val channelId = "console_hud_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "ConsoleHUD Gamepad Overlay",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Active virtual console gamepad controls"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val launchIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, channelId)
                .setContentTitle("ConsoleHUD Active")
                .setContentText("Virtual Gamepad overlay running over games")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentIntent(pendingIntent)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("ConsoleHUD Active")
                .setContentText("Virtual Gamepad overlay running over games")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentIntent(pendingIntent)
                .build()
        }

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun setupOverlayView() {
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        overlayComposeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@FloatingHudService)
            setViewTreeViewModelStoreOwner(this@FloatingHudService)
            setViewTreeSavedStateRegistryOwner(this@FloatingHudService)

            setContent {
                FloatingHudOverlayView(
                    profileFlow = activeProfile,
                    isEditModeFlow = isEditMode,
                    isHudVisibleFlow = isHudVisible,
                    onToggleEditMode = {
                        isEditMode.value = !isEditMode.value
                    },
                    onToggleVisibility = {
                        isHudVisible.value = !isHudVisible.value
                    },
                    onCloseService = {
                        stopSelf()
                    },
                    onButtonTriggered = { button ->
                        handleButtonPress(button)
                    },
                    onButtonPositionUpdated = { updatedBtn ->
                        val p = activeProfile.value ?: return@FloatingHudOverlayView
                        repository.updateButtonConfig(p.id, updatedBtn)
                    }
                )
            }
        }

        try {
            windowManager.addView(overlayComposeView, params)
            isOverlayAttached = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleButtonPress(button: HudButtonConfig) {
        val profile = activeProfile.value ?: return

        // Feedback
        if (profile.soundEnabled) {
            if (button.isTurbo) soundManager.playTurboSound() else soundManager.playClickSound()
        }
        hapticManager.vibrate(profile.hapticStrength)

        // Inject touch via accessibility service if available
        if (TouchAccessibilityService.isConnected()) {
            val displayMetrics = resources.displayMetrics
            val targetPxX = button.targetXRatio * displayMetrics.widthPixels
            val targetPxY = button.targetYRatio * displayMetrics.heightPixels
            TouchAccessibilityService.tap(targetPxX, targetPxY, 40L)
        }
    }

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        appViewModelStore.clear()
        serviceScope.cancel()

        if (isOverlayAttached && overlayComposeView != null) {
            try {
                windowManager.removeView(overlayComposeView)
            } catch (e: Exception) {
                // Ignore
            }
            overlayComposeView = null
            isOverlayAttached = false
        }
        soundManager.release()
        _isFloatingHudRunning.value = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val NOTIFICATION_ID = 4099
        private val _isFloatingHudRunning = MutableStateFlow(false)
        val isFloatingHudRunning: StateFlow<Boolean> = _isFloatingHudRunning.asStateFlow()

        fun start(context: Context) {
            val intent = Intent(context, FloatingHudService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, FloatingHudService::class.java)
            context.stopService(intent)
        }
    }
}

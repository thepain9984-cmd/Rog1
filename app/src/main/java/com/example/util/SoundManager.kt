package com.example.util

import android.media.AudioManager
import android.media.ToneGenerator

class SoundManager {

    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 55)
        } catch (e: Exception) {
            toneGenerator = null
        }
    }

    fun playClickSound() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 25)
        } catch (e: Exception) {
            // Ignore if tone cannot be played
        }
    }

    fun playTurboSound() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_SUP_PIP, 20)
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun playSpecialSound() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 40)
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (e: Exception) {
            // Ignore
        }
    }
}

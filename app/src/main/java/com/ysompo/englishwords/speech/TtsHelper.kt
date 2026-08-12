package com.ysompo.englishwords.speech

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import java.util.Locale

class TtsHelper(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var ready = false
    private val mainHandler = Handler(Looper.getMainLooper())
    private var pendingSpeak: Runnable? = null

    companion object {
        // Gives the child a moment to get ready/look at the screen after tapping "listen"
        // before the word is actually read aloud, instead of it starting instantly.
        private const val SPEAK_DELAY_MS = 500L
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            ready = true
        }
    }

    fun speak(word: String) {
        pendingSpeak?.let { mainHandler.removeCallbacks(it) }
        val runnable = Runnable {
            if (ready) {
                tts?.speak(word, TextToSpeech.QUEUE_FLUSH, null, "word_utterance")
            }
        }
        pendingSpeak = runnable
        mainHandler.postDelayed(runnable, SPEAK_DELAY_MS)
    }

    fun shutdown() {
        pendingSpeak?.let { mainHandler.removeCallbacks(it) }
        pendingSpeak = null
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}

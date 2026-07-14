package com.joseph.tellorakids.common.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

class TextToSpeechHelper(
    context: Context,
    private val onSpeechFinished: () -> Unit = {}
) {

    private var tts: TextToSpeech? = null
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking = _isSpeaking.asStateFlow()

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Try to find a warmer, more storytelling-friendly locale if available
                val result = tts?.setLanguage(Locale.US)
                
                // Storytelling Tuning
                tts?.setPitch(1.2f)       // Slightly higher pitch for a "kinder/softer" voice
                tts?.setSpeechRate(0.75f) // Significantly slower for dramatic effect and clarity
            }
        }

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
                onSpeechFinished()
            }

            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
            }
        })
    }

    fun speak(text: String) {
        // "Storytelling Magic": We add extra pauses at punctuation to make it feel human
        val storytellingText = text
            .replace(".", "... ")   // Long pause at end of sentence
            .replace(",", ", .. ")  // Short breath at commas
            .replace("!", "!!... ") // Excited pause
            .replace("?", "??... ") // Curious pause
            
        tts?.speak(storytellingText, TextToSpeech.QUEUE_FLUSH, null, "story_page_utterance")
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun shutdown() {
        tts?.shutdown()
    }
}

// core/platform/tts/TtsController.kt
package com.example.lookey.core.platform.tts

import android.content.Context
import android.media.AudioAttributes
import android.speech.tts.TextToSpeech
import java.util.Locale

class TtsController(private val context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var ready = false

    init { tts = TextToSpeech(context, this) }

    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS
        if (ready) {
            tts?.language = Locale.KOREAN
            tts?.setSpeechRate(1.0f)
            tts?.setPitch(1.0f)
            // ✅ 추천: 내비게이션/가이드 용도 채널
            tts?.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
        }
    }

    // ✅ flush 옵션 추가
    fun speak(text: String, flush: Boolean = true) {
        if (!ready) return
        val queue = if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        tts?.speak(text, queue, null, System.currentTimeMillis().toString())
    }

    fun stop() { tts?.stop() }
    fun shutdown() { tts?.shutdown() }
}

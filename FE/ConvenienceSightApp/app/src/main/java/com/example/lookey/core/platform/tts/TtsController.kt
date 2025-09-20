package com.example.lookey.core.platform.tts


import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TtsController(private val context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var ready = false

    init {
        // 초기화 시점에 TTS 엔진 로딩
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS
        if (ready) {
            tts?.language = Locale.KOREAN   // 기본 한국어
            tts?.setSpeechRate(1.0f)        // 말하기 속도
            tts?.setPitch(1.0f)             // 음 높이
        }
    }

    fun speak(text: String) {
        if (ready) {
            tts?.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                System.currentTimeMillis().toString()
            )
        }
    }

    fun shutdown() {
        tts?.shutdown()
    }

    fun stop() { tts?.stop() }

}
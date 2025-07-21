package com.example.myapplication.ai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

class AIRecognizer(
    private val context: Context,
    private val onResult: (String) -> Unit,
    private val onPartialResult: ((String) -> Unit)? = null,
    private val onError: ((String) -> Unit)? = null
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false

    fun startListening() {
        if (speechRecognizer == null) {
            try {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            } catch (e: Exception) {
                onError?.invoke("Speech recognition service not available on this device.")
                return
            }
            if (speechRecognizer == null) {
                onError?.invoke("Speech recognition service not available on this device.")
                return
            }
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        onPartialResult?.invoke(matches[0])
                    }
                }
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        onResult(matches[0])
                    }
                    isListening = false
                }
                override fun onError(error: Int) {
                    val msg = when (error) {
                        SpeechRecognizer.ERROR_NETWORK -> "Network error"
                        SpeechRecognizer.ERROR_NO_MATCH -> "No match"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Timeout"
                        else -> "Error code: $error"
                    }
                    onError?.invoke(msg)
                    isListening = false
                }
            })
        }
        if (!isListening) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            speechRecognizer?.startListening(intent)
            isListening = true
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        isListening = false
    }

    fun destroy() {
        try {
            speechRecognizer?.destroy()
        } catch (e: IllegalArgumentException) {
            // 已經被系統回收或未註冊，忽略
        }
        speechRecognizer = null
        isListening = false
    }
} 
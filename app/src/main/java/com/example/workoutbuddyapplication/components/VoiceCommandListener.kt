package com.example.workoutbuddyapplication.components

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun VoiceCommandListener(
    enabled: Boolean,
    stopWord: String,
    onFinishSet: () -> Unit
) {
    val context = LocalContext.current
    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    val intent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
        }
    }

    DisposableEffect(enabled, stopWord) {
        if (enabled) {
            val listener = object : RecognitionListener {
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (matches?.any { it.contains(stopWord, ignoreCase = true) } == true) {
                        onFinishSet()
                    }
                    speechRecognizer.startListening(intent)
                }

                override fun onError(error: Int) {
                    speechRecognizer.startListening(intent)
                }

                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            }
            speechRecognizer.setRecognitionListener(listener)
            speechRecognizer.startListening(intent)
        }
        onDispose { speechRecognizer.destroy() }
    }
}
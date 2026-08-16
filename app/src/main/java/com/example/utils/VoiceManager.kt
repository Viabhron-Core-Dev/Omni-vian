package com.example.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.Locale

object VoiceManager : TextToSpeech.OnInitListener {

    private const val PREFS_NAME = "omniroot_audio_prefs"
    private const val KEY_STT_ENGINE = "stt_engine"
    private const val KEY_SELECTED_MODEL = "selected_model_path"
    private const val KEY_TTS_PITCH = "tts_pitch"
    private const val KEY_TTS_SPEED = "tts_speed"

    const val ENGINE_ANDROID_NATIVE = "android_native"
    const val ENGINE_CUSTOM_MODEL = "custom_model"

    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    fun init(context: Context) {
        if (tts == null) {
            tts = TextToSpeech(context.applicationContext, this)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.getDefault()
            isTtsInitialized = true
            LogKeeper.log("VoiceManager", "TtsInitSuccess", "TTS initialized with locale: ${Locale.getDefault()}")
        } else {
            LogKeeper.log("VoiceManager", "TtsInitError", "TTS initialization failed with status: $status")
        }
    }

    fun getSttEngine(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_STT_ENGINE, ENGINE_ANDROID_NATIVE) ?: ENGINE_ANDROID_NATIVE
    }

    fun setSttEngine(context: Context, engine: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_STT_ENGINE, engine).apply()
        LogKeeper.log("VoiceManager", "EngineChanged", "STT Engine changed to: $engine")
    }

    fun getSelectedModelPath(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SELECTED_MODEL, null)
    }

    fun setSelectedModelPath(context: Context, path: String?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SELECTED_MODEL, path).apply()
    }

    fun getTtsPitch(context: Context): Float {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getFloat(KEY_TTS_PITCH, 1.0f)
    }

    fun setTtsPitch(context: Context, pitch: Float) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_TTS_PITCH, pitch).apply()
        tts?.setPitch(pitch)
    }

    fun getTtsSpeed(context: Context): Float {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getFloat(KEY_TTS_SPEED, 1.0f)
    }

    fun setTtsSpeed(context: Context, speed: Float) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_TTS_SPEED, speed).apply()
        tts?.setSpeechRate(speed)
    }

    fun getAudioModelsDir(context: Context): File {
        return File(context.filesDir, "audio_models").apply { mkdirs() }
    }

    fun listImportedModels(context: Context): List<File> {
        val dir = getAudioModelsDir(context)
        return dir.listFiles()?.filter { it.isFile && (it.name.endsWith(".bin") || it.name.endsWith(".onnx") || it.name.endsWith(".tflite")) } ?: emptyList()
    }

    fun startListening(
        context: Context,
        onPartialResult: (String) -> Unit = {},
        onFinalResult: (String) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("Speech recognition is not available on this device.")
            LogKeeper.log("VoiceManager", "STTUnavailable", "Speech recognition unavailable")
            return
        }

        stopListening()

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _isListening.value = true
                    LogKeeper.log("VoiceManager", "STTReady", "Ready for speech input")
                }

                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    _isListening.value = false
                }

                override fun onError(error: Int) {
                    _isListening.value = false
                    val errorMsg = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        SpeechRecognizer.ERROR_CLIENT -> "Client error"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient audio permissions"
                        SpeechRecognizer.ERROR_NETWORK -> "Network error"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer busy"
                        SpeechRecognizer.ERROR_SERVER -> "Server error"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                        else -> "Unknown error ($error)"
                    }
                    LogKeeper.log("VoiceManager", "STTError", errorMsg)
                    onError(errorMsg)
                }

                override fun onResults(results: Bundle?) {
                    _isListening.value = false
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull() ?: ""
                    LogKeeper.log("VoiceManager", "STTFinal", "Result: '$text'")
                    onFinalResult(text)
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull() ?: ""
                    onPartialResult(text)
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _isListening.value = false
            onError("Failed to start speech recognizer: ${e.message}")
            LogKeeper.log("VoiceManager", "STTException", "Exception starting STT: ${e.message}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (_: Exception) {}
        _isListening.value = false
    }

    fun speak(context: Context, text: String, onComplete: () -> Unit = {}) {
        if (!isTtsInitialized || tts == null) {
            init(context)
        }

        tts?.setPitch(getTtsPitch(context))
        tts?.setSpeechRate(getTtsSpeed(context))

        _isSpeaking.value = true
        LogKeeper.log("VoiceManager", "TTSSpeak", "Speaking text of length ${text.length}")

        val utteranceId = "omniroot_tts_${System.currentTimeMillis()}"
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stopSpeaking() {
        tts?.stop()
        _isSpeaking.value = false
    }
}

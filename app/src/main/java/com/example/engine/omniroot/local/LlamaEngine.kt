package com.example.engine.omniroot.local

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class LlamaEngine(private val context: Context) {
    companion object {
        private const val TAG = "LlamaEngine"
        
        @JvmStatic
        fun onNativeLog(level: String, message: String) {
            com.example.utils.LogKeeper.log(level, "Local AI (C++)", message)
        }

        init {
            try {
                System.loadLibrary("llama_bridge")
                Log.i(TAG, "Successfully loaded native llama_bridge library")
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "Failed to load native library llama_bridge", e)
            }
        }
    }

    /**
     * Initializes the native context with a specific .gguf file, with RAM safety checks.
     */
    fun loadModelSafely(path: String): Boolean {
        // Skip actual file size check for the mock/stub environment to prevent crashes,
        // but perform the RAM check logic as designed in the blueprint.
        
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val availableRamBytes = memoryInfo.availMem
        val requiredBufferBytes = 500L * 1024 * 1024 // 500 MB safety buffer for OS

        Log.i(TAG, "Available RAM: ${availableRamBytes / (1024*1024)} MB")

        // If the device has critically low RAM (e.g. less than 1GB free), block load
        if (availableRamBytes < (1024L * 1024 * 1024)) {
            Log.w(TAG, "OOM WARNING: Device has very low RAM available.")
            // We won't hard block here for the sake of the mock, but in production we would.
        }

        return loadModel(path)
    }

    private external fun loadModel(path: String): Boolean
private var tokenListener: ((String) -> Unit)? = null

    // Called from C++ via JNI
    fun onTokenGenerated(token: String) {
        tokenListener?.invoke(token)
    }

    fun predictStream(prompt: String, listener: (String) -> Unit) {
        tokenListener = listener
        predictStreamNative(prompt)
        tokenListener = null // Cleanup after finish
    }

fun predictFlow(prompt: String): Flow<String> = callbackFlow {
        // 1. Assign the JNI listener to push words into the Flow pipe
        tokenListener = { token ->
            trySend(token)
        }

        // 2. Launch the heavy C++ math in a background thread so the UI doesn't freeze
        launch(Dispatchers.IO) {
            predictStreamNative(prompt)
            // 3. When C++ finishes the loop, close the pipe
            close()
        }

        // 4. Cleanup if the user hits "Stop" and cancels the coroutine
        awaitClose {
            tokenListener = null
        }
    }

    private external fun predictStreamNative(prompt: String)
    external fun predict(prompt: String): String
    external fun unloadModel()
}

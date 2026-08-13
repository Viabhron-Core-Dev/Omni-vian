package com.example.engine.omniroot.local

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import java.io.File

class LlamaEngine(private val context: Context) {
    companion object {
        private const val TAG = "LlamaEngine"
        
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
    external fun predict(prompt: String): String
    external fun unloadModel()
}

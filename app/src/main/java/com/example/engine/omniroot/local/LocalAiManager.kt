package com.example.engine.omniroot.local

import android.content.Context
import com.example.utils.LogKeeper

object LocalAiManager {
    private var activeEngine: LlamaEngine? = null
    private var currentModelPath: String? = null

    fun getOrLoadEngine(context: Context, absolutePath: String): LlamaEngine? {
        if (activeEngine != null && currentModelPath == absolutePath) {
            LogKeeper.log("Local AI", "getOrLoadEngine", "Using cached RAM engine for $absolutePath")
            return activeEngine
        }
        
        LogKeeper.log("Local AI", "getOrLoadEngine", "Unloading previous model (if any) and loading new model into RAM: $absolutePath")
        activeEngine?.unloadModel()
        
        val engine = LlamaEngine(context.applicationContext)
        val loaded = engine.loadModelSafely(absolutePath)
        if (loaded) {
            activeEngine = engine
            currentModelPath = absolutePath
            LogKeeper.log("Local AI", "getOrLoadEngine", "Successfully loaded $absolutePath into RAM.")
            return engine
        } else {
            LogKeeper.log("Local AI", "ERROR", "Failed to load $absolutePath (OOM or File Not Found)")
            engine.unloadModel()
            activeEngine = null
            currentModelPath = null
            return null
        }
    }

    fun unload() {
        LogKeeper.log("Local AI", "unload", "Unloading model from RAM.")
        activeEngine?.unloadModel()
        activeEngine = null
        currentModelPath = null
    }
}

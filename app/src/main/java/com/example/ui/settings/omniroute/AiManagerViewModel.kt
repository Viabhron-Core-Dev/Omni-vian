package com.example.ui.settings.omniroute

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.db.AppDatabase
import com.example.engine.db.ApiProviderEntity
import com.example.engine.db.ApiKeyEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class AiManagerViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val apiProviderDao = db.apiProviderDao()
    private val apiKeyDao = db.apiKeyDao()
    private val metricsDao = db.metricsDao()

    val providers = apiProviderDao.getAllProviders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val activeKeys = apiKeyDao.getAllKeys()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableModels = activeKeys.map { keys ->
        val models = mutableListOf<String>()
        val providerIds = keys.map { it.providerId }.toSet()
        if (providerIds.contains("google_ai_studio")) {
            models.addAll(listOf("Gemini Pro Latest", "Gemini Flash"))
        }
        if (providerIds.contains("openai")) {
            models.addAll(listOf("GPT-4o", "GPT-4o Mini"))
        }
        if (providerIds.contains("anthropic")) {
            models.addAll(listOf("Claude 3.5 Sonnet", "Claude 3 Opus"))
        }
        if (providerIds.contains("openrouter")) {
            models.addAll(listOf("OpenRouter Llama 3 8B", "OpenRouter Mistral 7B"))
        }
        if (providerIds.contains("groq")) {
            models.addAll(listOf("Groq Llama 3 8B", "Groq Llama 3 70B"))
        }
        if (providerIds.contains("together_ai")) {
            models.addAll(listOf("Together Llama 3 8B"))
        }
        if (providerIds.contains("local_gguf")) {
            models.addAll(listOf("Local Llama 3"))
        }
        if (models.isEmpty()) {
            models.add("No API Keys configured")
        }
        models
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("Loading..."))

    val totalTokens = metricsDao.getTotalTokensUsed()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
        
    val totalRequests = metricsDao.getTotalRequestCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
        
    val totalCost = metricsDao.getTotalEstimatedCost()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun addMockKey(providerId: String) {
        viewModelScope.launch {
            apiKeyDao.insertKey(
                ApiKeyEntity(
                    id = UUID.randomUUID().toString(),
                    providerId = providerId,
                    alias = "Test Key " + UUID.randomUUID().toString().take(4),
                    keyMasked = "sk-...abcd",
                    keyValue = "fake-key",
                    isActive = true,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun saveRealKey(providerId: String, alias: String, keyValue: String) {
        viewModelScope.launch {
            val keyMasked = if (keyValue.length > 8) {
                "sk-..." + keyValue.takeLast(4)
            } else {
                "sk-***"
            }
            apiKeyDao.insertKey(
                ApiKeyEntity(
                    id = UUID.randomUUID().toString(),
                    providerId = providerId,
                    alias = alias,
                    keyMasked = keyMasked,
                    keyValue = keyValue,
                    isActive = true,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }
}

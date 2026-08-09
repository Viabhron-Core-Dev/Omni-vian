package com.example.ui.settings.omniroute

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.db.AppDatabase
import com.example.engine.db.ApiProviderEntity
import com.example.engine.db.ApiKeyEntity
import kotlinx.coroutines.flow.SharingStarted
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

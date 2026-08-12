package com.example.engine.db

import androidx.room.Entity

@Entity(tableName = "ai_models", primaryKeys = ["providerId", "modelId"])
data class AiModelEntity(
    val providerId: String,
    val modelId: String,
    val fetchedAt: Long = System.currentTimeMillis()
)

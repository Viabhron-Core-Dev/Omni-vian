package com.example.engine.omniroot.pipeline

import com.example.ui.chat.OmniMessage
import com.example.ui.chat.OmniRequest
import com.example.ui.chat.OmniResponse
import com.example.ui.chat.OmniChoice
import org.json.JSONArray
import org.json.JSONObject

object TranslationEngine {

    enum class ProviderFormat {
        OPENAI,
        ANTHROPIC,
        GEMINI
    }

    fun translateRequest(request: OmniRequest, targetFormat: ProviderFormat): String {
        return when (targetFormat) {
            ProviderFormat.OPENAI -> {
                val json = JSONObject()
                json.put("model", request.model)
                val messagesArray = JSONArray()
                request.messages.forEach { msg ->
                    val msgObj = JSONObject()
                    msgObj.put("role", msg.role)
                    msgObj.put("content", msg.content)
                    messagesArray.put(msgObj)
                }
                json.put("messages", messagesArray)
                json.toString(2)
            }
            ProviderFormat.ANTHROPIC -> {
                val json = JSONObject()
                json.put("model", request.model)
                json.put("max_tokens", 4096)
                
                var systemPrompt = ""
                val messagesArray = JSONArray()
                request.messages.forEach { msg ->
                    if (msg.role == "system") {
                        systemPrompt += msg.content + "\n"
                    } else {
                        val msgObj = JSONObject()
                        val role = if (msg.role == "user") "user" else "assistant"
                        msgObj.put("role", role)
                        msgObj.put("content", msg.content)
                        messagesArray.put(msgObj)
                    }
                }
                if (systemPrompt.isNotEmpty()) {
                    json.put("system", systemPrompt.trim())
                }
                json.put("messages", messagesArray)
                json.toString(2)
            }
            ProviderFormat.GEMINI -> {
                val json = JSONObject()
                val contentsArray = JSONArray()
                
                var systemInstruction = ""
                
                request.messages.forEach { msg ->
                    if (msg.role == "system") {
                        systemInstruction += msg.content + "\n"
                    } else {
                        val contentObj = JSONObject()
                        val role = if (msg.role == "user") "user" else "model"
                        contentObj.put("role", role)
                        val partsArray = JSONArray()
                        val partObj = JSONObject()
                        partObj.put("text", msg.content)
                        partsArray.put(partObj)
                        contentObj.put("parts", partsArray)
                        contentsArray.put(contentObj)
                    }
                }
                
                if (systemInstruction.isNotEmpty()) {
                    val sysInstructionObj = JSONObject()
                    val sysPartsArray = JSONArray()
                    val sysPartObj = JSONObject()
                    sysPartObj.put("text", systemInstruction.trim())
                    sysPartsArray.put(sysPartObj)
                    sysInstructionObj.put("parts", sysPartsArray)
                    json.put("systemInstruction", sysInstructionObj)
                }
                
                json.put("contents", contentsArray)
                json.toString(2)
            }
        }
    }

    fun translateResponse(rawResponse: String, sourceFormat: ProviderFormat): OmniResponse {
        return try {
            val json = JSONObject(rawResponse)
            when (sourceFormat) {
                ProviderFormat.OPENAI -> {
                    val choices = json.optJSONArray("choices")
                    val content = choices?.optJSONObject(0)?.optJSONObject("message")?.optString("content") ?: ""
                    OmniResponse(choices = listOf(OmniChoice(message = OmniMessage(role = "assistant", content = content))))
                }
                ProviderFormat.ANTHROPIC -> {
                    val contentArray = json.optJSONArray("content")
                    val content = contentArray?.optJSONObject(0)?.optString("text") ?: ""
                    OmniResponse(choices = listOf(OmniChoice(message = OmniMessage(role = "assistant", content = content))))
                }
                ProviderFormat.GEMINI -> {
                    val candidates = json.optJSONArray("candidates")
                    val parts = candidates?.optJSONObject(0)?.optJSONObject("content")?.optJSONArray("parts")
                    val content = parts?.optJSONObject(0)?.optString("text") ?: ""
                    OmniResponse(choices = listOf(OmniChoice(message = OmniMessage(role = "assistant", content = content))))
                }
            }
        } catch (e: Exception) {
            OmniResponse(choices = listOf(OmniChoice(message = OmniMessage(role = "assistant", content = "Error translating response: ${e.message}"))))
        }
    }
}

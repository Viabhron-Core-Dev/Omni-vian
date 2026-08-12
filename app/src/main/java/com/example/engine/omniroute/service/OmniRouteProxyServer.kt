package com.example.engine.omniroute.service

import android.content.Context
import android.util.Log
import fi.iki.elonen.NanoHTTPD
import java.io.IOException
import com.example.engine.omniroute.pipeline.TranslationEngine
import com.example.ui.chat.OmniRequest
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import com.example.engine.db.AppDatabase
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import java.util.concurrent.TimeUnit
import com.example.utils.LogKeeper

class OmniRouteProxyServer(port: Int, private val context: Context) : NanoHTTPD("127.0.0.1", port) {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val requestAdapter = moshi.adapter(OmniRequest::class.java)
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    init {
        Log.d("OmniRouteProxyServer", "Initializing OmniRoute Proxy on 127.0.0.1:$port")
    }

    override fun serve(session: IHTTPSession): Response {
        if (session.uri == "/v1/chat/completions" && session.method == Method.POST) {
            try {
                val map = HashMap<String, String>()
                session.parseBody(map)
                val postData = map["postData"] ?: return newFixedLengthResponse(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "Missing POST data")
                
                val request = requestAdapter.fromJson(postData) ?: return newFixedLengthResponse(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "Invalid JSON")
                
                val modelStr = request.model ?: "google_ai_studio/gemini-1.5-pro-latest"
                val slashIdx = modelStr.indexOf('/')
                
                val providerId = if (slashIdx != -1) modelStr.substring(0, slashIdx) else "google_ai_studio"
                val actualModelName = if (slashIdx != -1) modelStr.substring(slashIdx + 1) else modelStr

                // Determine target format and base url dynamically from provider pre-populator
                val (targetFormat, baseUrl) = when (providerId) {
                    "google_ai_studio" -> Pair(TranslationEngine.ProviderFormat.OPENAI, "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions")
                    "openai" -> Pair(TranslationEngine.ProviderFormat.OPENAI, "https://api.openai.com/v1/chat/completions")
                    "anthropic" -> Pair(TranslationEngine.ProviderFormat.ANTHROPIC, "https://api.anthropic.com/v1/messages")
                    "openrouter" -> Pair(TranslationEngine.ProviderFormat.OPENAI, "https://openrouter.ai/api/v1/chat/completions")
                    "groq" -> Pair(TranslationEngine.ProviderFormat.OPENAI, "https://api.groq.com/openai/v1/chat/completions")
                    "together_ai" -> Pair(TranslationEngine.ProviderFormat.OPENAI, "https://api.together.xyz/v1/chat/completions")
                    "local_gguf" -> Pair(TranslationEngine.ProviderFormat.OPENAI, "http://localhost:8080/v1/chat/completions")
                    else -> Pair(TranslationEngine.ProviderFormat.OPENAI, "https://api.openai.com/v1/chat/completions")
                }

                val db = AppDatabase.getDatabase(context)
                val activeKeys = runBlocking { db.apiKeyDao().getKeysForProvider(providerId).first() }
                val key = activeKeys.firstOrNull { it.isActive }
                
                if (key == null && providerId != "local_gguf") {
                    val errorResponse = com.example.ui.chat.OmniResponse(choices = listOf(com.example.ui.chat.OmniChoice(message = com.example.ui.chat.OmniMessage(role = "assistant", content = "Error: No active API key found for $providerId"))))
                    val errorJson = Moshi.Builder().build().adapter(com.example.ui.chat.OmniResponse::class.java).toJson(errorResponse)
                    return newFixedLengthResponse(Response.Status.OK, "application/json", errorJson)
                }
                
                // Update the request with the actual model name for translation
                val updatedRequest = request.copy(model = actualModelName)
                
                val translatedPayload = TranslationEngine.translateRequest(updatedRequest, targetFormat)
                
                LogKeeper.log("Proxy", "Routing request to $providerId ($actualModelName)", translatedPayload)
                
                val reqBuilder = Request.Builder()
                
                when (targetFormat) {
                    TranslationEngine.ProviderFormat.GEMINI -> {
                        reqBuilder.url(baseUrl + "?key=" + (key?.keyValue ?: ""))
                    }
                    TranslationEngine.ProviderFormat.OPENAI -> {
                        reqBuilder.url(baseUrl)
                        reqBuilder.addHeader("Authorization", "Bearer " + (key?.keyValue ?: ""))
                        if (providerId == "openrouter") {
                            reqBuilder.addHeader("HTTP-Referer", "http://localhost:8080")
                            reqBuilder.addHeader("X-Title", "OmniRoute")
                        }
                    }
                    TranslationEngine.ProviderFormat.ANTHROPIC -> {
                        reqBuilder.url(baseUrl)
                        reqBuilder.addHeader("x-api-key", key?.keyValue ?: "")
                        reqBuilder.addHeader("anthropic-version", "2023-06-01")
                    }
                }
                
                val req = reqBuilder.post(translatedPayload.toRequestBody("application/json".toMediaType())).build()
                
                val response = httpClient.newCall(req).execute()
                val responseBody = response.body?.string() ?: ""
                
                if (!response.isSuccessful) {
                    LogKeeper.log("Proxy Error", "API Error from $providerId", "Code: ${response.code}\nBody: $responseBody")
                    val errorResponse = com.example.ui.chat.OmniResponse(choices = listOf(com.example.ui.chat.OmniChoice(message = com.example.ui.chat.OmniMessage(role = "assistant", content = "API Error ${response.code}: $responseBody"))))
                    val errorJson = Moshi.Builder().build().adapter(com.example.ui.chat.OmniResponse::class.java).toJson(errorResponse)
                    return newFixedLengthResponse(Response.Status.OK, "application/json", errorJson)
                }
                
                LogKeeper.log("Proxy", "Received response from $providerId", "Code: ${response.code}\nBody length: ${responseBody.length}")
                
                val standardResponse = TranslationEngine.translateResponse(responseBody, targetFormat)
                
                val moshiResponse = Moshi.Builder().build().adapter(com.example.ui.chat.OmniResponse::class.java).toJson(standardResponse)
                
                return newFixedLengthResponse(Response.Status.OK, "application/json", moshiResponse)
                
            } catch (e: Exception) {
                Log.e("OmniRouteProxyServer", "Error processing request", e)
                LogKeeper.log("Proxy Error", "Exception in proxy", e.message ?: "Unknown error", e.stackTraceToString())
                val errorResponse = com.example.ui.chat.OmniResponse(choices = listOf(com.example.ui.chat.OmniChoice(message = com.example.ui.chat.OmniMessage(role = "assistant", content = "Proxy Exception: ${e.message}"))))
                val errorJson = Moshi.Builder().build().adapter(com.example.ui.chat.OmniResponse::class.java).toJson(errorResponse)
                return newFixedLengthResponse(Response.Status.OK, "application/json", errorJson)
            }
        }
        
        return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not Found")
    }
}

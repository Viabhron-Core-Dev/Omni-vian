package com.example.engine.omniroute.service

import android.content.Context
import android.util.Log
import fi.iki.elonen.NanoHTTPD
import java.io.IOException
import com.example.engine.omniroute.pipeline.TranslationEngine
import com.example.ui.chat.OmniRequest
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OmniRouteProxyServer(port: Int, private val context: Context) : NanoHTTPD("127.0.0.1", port) {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val requestAdapter = moshi.adapter(OmniRequest::class.java)

    init {
        Log.d("OmniRouteProxyServer", "Initializing OmniRoute Proxy on 127.0.0.1:$port")
    }

    override fun serve(session: IHTTPSession): Response {
        if (session.uri == "/v1/chat/completions" && session.method == Method.POST) {
            try {
                val map = HashMap<String, String>()
                session.parseBody(map)
                val postData = map["postData"] ?: return newFixedLengthResponse(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "Missing POST data")
                
                // For Phase 9.7, we just return a stub response for now
                // In Phase 9.5 we will pipe this to the Combo Engine / Agent Shifter
                
                val responseJson = """
                    {
                        "choices": [
                            {
                                "message": {
                                    "role": "assistant",
                                    "content": "Hello from OmniRoute Local Proxy!"
                                }
                            }
                        ]
                    }
                """.trimIndent()
                
                return newFixedLengthResponse(Response.Status.OK, "application/json", responseJson)
            } catch (e: Exception) {
                Log.e("OmniRouteProxyServer", "Error parsing request", e)
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Internal Server Error: ${e.message}")
            }
        }
        
        return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not Found")
    }
}

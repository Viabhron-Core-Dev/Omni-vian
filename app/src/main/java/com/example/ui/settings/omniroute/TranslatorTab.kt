package com.example.ui.settings.omniroute

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.engine.omniroute.pipeline.CompressionEngine
import com.example.engine.omniroute.pipeline.TranslationEngine
import com.example.ui.chat.OmniMessage
import com.example.ui.chat.OmniRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslatorTab() {
    var inputPayload by remember { mutableStateOf("{\n  \"model\": \"omni-default\",\n  \"messages\": [\n    {\"role\": \"user\", \"content\": \"Hello, could you please tell me a joke?\"}\n  ]\n}") }
    var translatedPayload by remember { mutableStateOf("") }
    
    var selectedFormat by remember { mutableStateOf(TranslationEngine.ProviderFormat.GEMINI) }
    var compressionLevel by remember { mutableStateOf(CompressionEngine.CompressionLevel.NONE) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("OpenAI Format (Input)", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = inputPayload,
                onValueChange = { inputPayload = it },
                modifier = Modifier.fillMaxWidth().height(200.dp),
                textStyle = MaterialTheme.typography.bodySmall
            )
        }
        
        item {
            Text("Settings", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TranslationEngine.ProviderFormat.values().forEach { format ->
                    FilterChip(
                        selected = selectedFormat == format,
                        onClick = { selectedFormat = format },
                        label = { Text(format.name) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text("Compression Level", style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CompressionEngine.CompressionLevel.values().forEach { level ->
                    FilterChip(
                        selected = compressionLevel == level,
                        onClick = { compressionLevel = level },
                        label = { Text(level.name) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                try {
                    val json = org.json.JSONObject(inputPayload)
                    val model = json.optString("model", "default")
                    val messagesArray = json.optJSONArray("messages")
                    val messages = mutableListOf<OmniMessage>()
                    
                    if (messagesArray != null) {
                        for (i in 0 until messagesArray.length()) {
                            val msgObj = messagesArray.getJSONObject(i)
                            val originalContent = msgObj.getString("content")
                            val compressedContent = CompressionEngine.compress(originalContent, compressionLevel)
                            messages.add(OmniMessage(
                                role = msgObj.getString("role"),
                                content = compressedContent
                            ))
                        }
                    }
                    
                    val request = OmniRequest(model = model, messages = messages)
                    translatedPayload = TranslationEngine.translateRequest(request, selectedFormat)
                    
                } catch (e: Exception) {
                    translatedPayload = "Error parsing input JSON: ${e.message}"
                }
            }) {
                Text("Translate & Compress")
            }
        }
        
        item {
            Text("Translated Payload (Output)", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = translatedPayload,
                onValueChange = { },
                modifier = Modifier.fillMaxWidth().height(250.dp),
                textStyle = MaterialTheme.typography.bodySmall,
                readOnly = true
            )
        }
    }
}

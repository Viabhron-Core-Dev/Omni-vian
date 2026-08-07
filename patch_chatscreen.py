import re

with open('app/src/main/java/com/example/ui/chat/ChatScreen.kt', 'r') as f:
    content = f.read()

# Add icons
content = content.replace('import androidx.compose.material.icons.filled.ArrowUpward', 'import androidx.compose.material.icons.filled.ArrowUpward\nimport androidx.compose.material.icons.filled.Stop')

# Add isGenerating state
state_block = """    var showArtifactsList by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }"""

new_state_block = """    var showArtifactsList by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var currentJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }"""
content = content.replace(state_block, new_state_block)

# Replace the send button logic
old_send_btn = """                        FilledIconButton(
                            onClick = {
                                if (inputText.isNotBlank()) {
                                    val prompt = inputText
                                    chatMessages.add(ChatMessage(text = prompt, role = MessageRole.USER))
                                    inputText = ""
                                    
                                    val generatingMessage = ChatMessage(text = "Thinking...", role = MessageRole.AI)
                                    chatMessages.add(generatingMessage)
                                    
                                    scope.launch {
                                        val response = com.example.ui.chat.GeminiClient.generateContent(prompt)
                                        val index = chatMessages.indexOf(generatingMessage)
                                        if (index != -1) {
                                            chatMessages[index] = generatingMessage.copy(text = response)
                                        } else {
                                            chatMessages.add(ChatMessage(text = response, role = MessageRole.AI))
                                        }
                                    }
                                }
                            },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = "Send")
                        }"""

new_send_btn = """                        FilledIconButton(
                            onClick = {
                                if (isGenerating) {
                                    currentJob?.cancel()
                                    isGenerating = false
                                } else if (inputText.isNotBlank()) {
                                    val prompt = inputText
                                    chatMessages.add(ChatMessage(text = prompt, role = MessageRole.USER))
                                    inputText = ""
                                    
                                    val generatingMessage = ChatMessage(text = "Thinking...", role = MessageRole.AI)
                                    chatMessages.add(generatingMessage)
                                    
                                    isGenerating = true
                                    currentJob = scope.launch {
                                        try {
                                            val response = com.example.ui.chat.GeminiClient.generateContent(prompt)
                                            val index = chatMessages.indexOf(generatingMessage)
                                            if (index != -1) {
                                                chatMessages.removeAt(index)
                                            }
                                            
                                            if (response.actions.isNotEmpty() || response.editedFiles.isNotEmpty()) {
                                                chatMessages.add(ChatMessage(
                                                    text = "", 
                                                    role = MessageRole.APP_ACTION,
                                                    appActions = response.actions,
                                                    editedFiles = response.editedFiles
                                                ))
                                            }
                                            if (!response.text.isNullOrBlank()) {
                                                chatMessages.add(ChatMessage(text = response.text, role = MessageRole.AI))
                                            }
                                        } catch (e: kotlinx.coroutines.CancellationException) {
                                            val index = chatMessages.indexOf(generatingMessage)
                                            if (index != -1) {
                                                chatMessages[index] = generatingMessage.copy(text = "Generation stopped.")
                                            }
                                        } finally {
                                            isGenerating = false
                                        }
                                    }
                                }
                            },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = if (isGenerating) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.size(40.dp)
                        ) {
                            if (isGenerating) {
                                Icon(Icons.Default.Stop, contentDescription = "Stop", tint = MaterialTheme.colorScheme.error)
                            } else {
                                Icon(Icons.Default.ArrowUpward, contentDescription = "Send")
                            }
                        }"""

content = content.replace(old_send_btn, new_send_btn)

with open('app/src/main/java/com/example/ui/chat/ChatScreen.kt', 'w') as f:
    f.write(content)


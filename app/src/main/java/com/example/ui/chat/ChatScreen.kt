package com.example.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.List

import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.settings.omniroot.AiManagerViewModel
import androidx.compose.ui.graphics.Color
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.first
import com.example.engine.omniroot.local.LocalAiManager
import com.example.utils.LogKeeper
import kotlinx.coroutines.flow.onCompletion
import com.example.engine.omniroot.local.LlamaEngine
import kotlinx.coroutines.flow.first
import com.example.engine.db.AppDatabase
import com.example.engine.db.toEntity
import com.example.engine.db.toDomainModel
import kotlinx.coroutines.launch
import com.example.engine.server.PreviewServerManager
import java.io.File
import java.util.UUID

enum class MessageRole {
    USER, AI, APP_ACTION
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val role: MessageRole = MessageRole.USER,
    val modelName: String? = null,
    val providerId: String? = null,
    val editedFiles: List<Pair<String, Boolean>> = emptyList(),

    val appActions: List<String> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    sessionId: String,
    onMenuClick: () -> Unit
) {
    val aiViewModel: AiManagerViewModel = viewModel()
    val availableModels by aiViewModel.availableModels.collectAsState()
    
    val workspaceName = remember { mutableStateOf(com.example.engine.fs.LocalFileManager.getWorkspaceName(com.example.engine.fs.LocalFileManager.getWorkspaceDir().name)) }
    var inputText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isServerRunning by PreviewServerManager.isRunning.collectAsState()

    var showAgentSettings by remember { mutableStateOf(false) }
    var showTokenPanel by remember { mutableStateOf(false) }
    var showAttachmentPicker by remember { mutableStateOf(false) }
    var selectedFile by remember { mutableStateOf<String?>(null) }
    var showArtifactsList by remember { mutableStateOf(false) }
    var isGenerating by remember { mutableStateOf(false) }
    var currentJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    
    val chatMessages = remember {
        mutableStateListOf<ChatMessage>()
    }

    val db = AppDatabase.getDatabase(context)
    val dao = db.chatMessageDao()
    
    LaunchedEffect(sessionId) {
        val initialMessages = dao.getMessagesForSession(sessionId).first()
        chatMessages.clear()
        chatMessages.addAll(initialMessages.map { it.toDomainModel() })
    }


    
    fun saveMessage(msg: ChatMessage) {
        if (msg.text != "Thinking...") {
            scope.launch { dao.insertMessage(msg.toEntity(sessionId)) }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        TopAppBar(
            title = { 
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { showAgentSettings = true }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(workspaceName.value, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Agent Settings", modifier = Modifier.size(16.dp))
                }
            },
            navigationIcon = {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.Menu, "Menu")
                }
            },
            actions = {
                var showMenu by remember { mutableStateOf(false) }
                var showRename by remember { mutableStateOf(false) }

                IconButton(onClick = {
                    showArtifactsList = true
                }) {
                    Icon(
                        Icons.Default.Code,
                        contentDescription = "Artifacts"
                    )
                }
                
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        onClick = { showMenu = false; showRename = true }
                    )
                    DropdownMenuItem(
                        text = { Text("AI Token Panel") },
                        onClick = { showMenu = false; showTokenPanel = true }
                    )
                    DropdownMenuItem(
                        text = { Text("Archive (GDrive)") },
                        onClick = { 
                            showMenu = false
                            android.widget.Toast.makeText(context, "Archive requires Google Drive integration", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = { showMenu = false }
                    )
                }

                if (showRename) {
                    var newName by remember { mutableStateOf(workspaceName.value) }
                    AlertDialog(
                        onDismissRequest = { showRename = false },
                        title = { Text("Rename Chat") },
                        text = { 
                            OutlinedTextField(
                                value = newName,
                                onValueChange = { newName = it },
                                label = { Text("Chat Name") }
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                workspaceName.value = newName
                                showRename = false
                            }) {
                                Text("Save")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showRename = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        )
        
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(chatMessages.size, key = { chatMessages[it].id }) { index ->
                val message = chatMessages[index]
                val isLastMessage = index == chatMessages.lastIndex
                when (message.role) {
                    MessageRole.USER -> UserMessage(text = message.text, isLastMessage = isLastMessage)
                    MessageRole.AI -> AiMessage(message = message, isLastMessage = isLastMessage, aiViewModel = aiViewModel)
                    MessageRole.APP_ACTION -> AppActionMessage(
                        editedFiles = message.editedFiles,
                        appActions = message.appActions,
                        onFileClick = { selectedFile = it },
                        isLastMessage = isLastMessage
                    )
                }
            }
        }

        // Chat Input Box
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Make changes, add new features, ask for anything") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 120.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    maxLines = Int.MAX_VALUE
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Agent/Model Selector Pill
                    var showModelPicker by remember { mutableStateOf(false) }
                    var selectedModel by remember { mutableStateOf("Select Model") }
                    
                    // Update selected model if it's not in the available models list
                    LaunchedEffect(availableModels) {
                        if (availableModels.isNotEmpty() && !availableModels.contains(selectedModel)) {
                            selectedModel = availableModels.first()
                        }
                    }
                    
                    Box {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.clickable { showModelPicker = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val displayInitials = remember(selectedModel) {
                                    if (selectedModel == "Select Model" || selectedModel.startsWith("No models") || selectedModel.startsWith("Loading")) {
                                        "AI"
                                    } else if (selectedModel.contains("/")) {
                                        val parts = selectedModel.split("/", limit = 2)
                                        val p = parts[0].firstOrNull()?.uppercaseChar() ?: '?'
                                        val m = parts[1].firstOrNull()?.uppercaseChar() ?: '?'
                                        "$p / $m"
                                    } else {
                                        selectedModel.take(2).uppercase()
                                    }
                                }
                                Text(displayInitials, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Model", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                        
                        DropdownMenu(
                            expanded = showModelPicker,
                            onDismissRequest = { showModelPicker = false }
                        ) {
                            availableModels.forEach { modelName ->
                                DropdownMenuItem(
                                    text = { Text(modelName) },
                                    onClick = { 
                                        selectedModel = modelName
                                        showModelPicker = false 
                                    }
                                )
                            }
                        }
                    }

                    // Action Buttons
                    Row(
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledIconButton(
                            onClick = { showAttachmentPicker = true },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        FilledIconButton(
                            onClick = {
                                if (isGenerating) {
                                    currentJob?.cancel()
                                    isGenerating = false
                                } else if (inputText.isNotBlank()) {
                                    val prompt = inputText
                                    val msg = ChatMessage(text = prompt, role = MessageRole.USER)
                                    chatMessages.add(msg)
                                    saveMessage(msg)
                                    inputText = ""
                                    

                                    val parts = selectedModel.split("/", limit = 2)
                                    var currentProvider = parts.getOrNull(0)
                                    var currentModel = parts.getOrNull(1) ?: selectedModel
                                    
                                    if (currentProvider == "Select Model" || currentProvider == null) {
                                        currentProvider = "google_ai_studio"
                                        currentModel = "gemini-1.5-pro-latest"
                                    }

                                    val loadingText = if (currentProvider == "local_gguf") "Waking up $currentModel in RAM..." else "Thinking..."
                                    val generatingMessage = ChatMessage(text = loadingText, role = MessageRole.AI, modelName = currentModel, providerId = currentProvider)

                                    chatMessages.add(generatingMessage)
                                    
                                    isGenerating = true
                                    currentJob = scope.launch {
                                        try {
                                            if (currentProvider == "local_gguf") {
                                                // Mini-Phase 3 & 4: Direct Bypass and Streaming UI
                                                val models = db.aiModelDao().getAllModels().first()
                                                val modelEntity = models.firstOrNull { it.providerId == "local_gguf" && it.modelId == currentModel }
                                                val absolutePath = modelEntity?.description ?: currentModel
                                                
                                                val llama = LocalAiManager.getOrLoadEngine(context, absolutePath)
                                                
                                                if (llama != null) {
                                                    var combinedPrompt = ""
                                                    chatMessages.filter { it.id != generatingMessage.id }.forEach { msg ->
                                                        val roleStr = if (msg.role == MessageRole.USER) "user" else "assistant"
                                                        combinedPrompt += "<|im_start|>$roleStr\n${msg.text}<|im_end|>\n"
                                                    }
                                                    combinedPrompt += "<|im_start|>assistant\n"
                                                    
                                                    var streamedText = ""
                                                    val startTime = System.currentTimeMillis()
                                                    var tokenCount = 0
                                                    
                                                    llama.predictFlow(combinedPrompt).collect { token ->
                                                        streamedText += token
                                                        tokenCount++
                                                        
                                                        val index = chatMessages.indexOfFirst { it.id == generatingMessage.id }
                                                        if (index != -1) {
                                                            chatMessages[index] = generatingMessage.copy(text = streamedText)
                                                        }
                                                    }
                                                    
                                                    val endTime = System.currentTimeMillis()
                                                    val elapsedSec = (endTime - startTime) / 1000.0
                                                    val tps = if (elapsedSec > 0) tokenCount / elapsedSec else 0.0
                                                    LogKeeper.log("Local AI", "Metrics", "Stream finished. Tokens: $tokenCount, Time: ${elapsedSec}s, TPS: $tps")
                                                    
                                                    // Note: We DO NOT unloadModel() here anymore. We keep it alive in LocalAiManager!
                                                    
                                                    // Final save
                                                    val index = chatMessages.indexOfFirst { it.id == generatingMessage.id }
                                                    if (index != -1) {
                                                        val finalMsg = chatMessages[index]
                                                        saveMessage(finalMsg)
                                                    }
                                                } else {
                                                    val index = chatMessages.indexOfFirst { it.id == generatingMessage.id }
                                                    if (index != -1) {
                                                        val finalMsg = generatingMessage.copy(text = "Error: Local model failed to load (OOM or File Not Found).")
                                                        chatMessages[index] = finalMsg
                                                        saveMessage(finalMsg)
                                                    }
                                                }
                                                
                                            } else {
                                                // Normal OmniRoot HTTP Proxy flow
                                                val response = com.example.ui.chat.OmniRootClient.generateContent(
                                                    chatMessages.filter { it.id != generatingMessage.id },
                                                    selectedModel
                                                )
                                                val index = chatMessages.indexOf(generatingMessage)
                                                if (index != -1) {
                                                    chatMessages.removeAt(index)
                                                }
                                                
                                                if (response.actions.isNotEmpty() || response.editedFiles.isNotEmpty()) {
                                                    val msg = ChatMessage(text = "", role = MessageRole.APP_ACTION, appActions = response.actions, editedFiles = response.editedFiles)
                                                    chatMessages.add(msg)
                                                    saveMessage(msg)
                                                }

                                                if (!response.text.isNullOrBlank()) {
                                                    val msg = ChatMessage(text = response.text, role = MessageRole.AI, modelName = currentModel, providerId = currentProvider)

                                                    chatMessages.add(msg)
                                                    saveMessage(msg)
                                                }
                                            }
                                        } catch (e: kotlinx.coroutines.CancellationException) {
                                            val index = chatMessages.indexOfFirst { it.id == generatingMessage.id }
                                            if (index != -1) {
                                                val oldText = chatMessages[index].text
                                                val msg = chatMessages[index].copy(text = if (oldText.isBlank() || oldText.contains("Waking up")) "Generation stopped." else oldText)
                                                chatMessages[index] = msg
                                                saveMessage(msg)
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
                        }
                    }
                }
            }
        }
        
        if (showAgentSettings) {
            AgentSettingsBottomSheet(
                onDismiss = { showAgentSettings = false }
            )
        }
        
        if (showTokenPanel) {
            AiTokenPanelBottomSheet(
                onDismiss = { showTokenPanel = false }
            )
        }
        
        selectedFile?.let { fileName ->
            FileAttachmentBottomSheet(
                fileName = fileName,
                onDismiss = { selectedFile = null }
            )
        }
        
        var selectedArtifact by remember { mutableStateOf<ArtifactItem?>(null) }
        
        if (showArtifactsList) {
            ArtifactsListBottomSheet(
                onDismiss = { showArtifactsList = false },
                onArtifactSelected = { artifact ->
                    selectedArtifact = artifact
                    showArtifactsList = false
                }
            )
        }
        
        selectedArtifact?.let { artifact ->
            PWAPreviewBottomSheet(
                url = artifact.url,
                title = artifact.name,
                onDismiss = { selectedArtifact = null }
            )
        }

        if (showAttachmentPicker) {
            AttachmentPickerBottomSheet(
                onDismiss = { showAttachmentPicker = false },
                onOptionSelected = { option ->
                    // Handle attachment logic here
                    when(option) {
                        is AttachmentOption.ImageUri -> {
                            val msg = ChatMessage(text = "Selected image: ${option.uri}", role = MessageRole.USER)
                            chatMessages.add(msg)
                            saveMessage(msg)
                        }
                        is AttachmentOption.FileUri -> {
                            val msg = ChatMessage(text = "Selected file: ${option.uri}", role = MessageRole.USER)
                            chatMessages.add(msg)
                            saveMessage(msg)
                            scope.launch {
                                val result = com.example.engine.fs.TextExtractor.extractTextFromUri(context, option.uri)
                                if (result.isSuccess) {
                                    val text = result.getOrNull()
                                    val msg = ChatMessage(text = "File content extracted (${text?.length} chars)", role = MessageRole.APP_ACTION)
                                    chatMessages.add(msg)
                                    saveMessage(msg)
                                } else {
                                    val msg = ChatMessage(text = "Failed to extract text", role = MessageRole.APP_ACTION)
                                    chatMessages.add(msg)
                                    saveMessage(msg)
                                }
                            }
                        }
                        is AttachmentOption.GithubRepo -> {
                            val msg = ChatMessage(text = "Importing repo: ${option.url} ...", role = MessageRole.USER)
                            chatMessages.add(msg)
                            saveMessage(msg)
                            scope.launch {
                                val repoName = option.url.trim().removeSuffix("/").substringAfterLast("/")
                                val destFolder = java.io.File(com.example.engine.fs.LocalFileManager.getWorkspaceDir(), repoName)
                                val destZip = java.io.File(com.example.engine.fs.LocalFileManager.getWorkspaceDir(), "$repoName.zip")
                                val result = com.example.engine.fs.GithubDownloader.downloadRepoAsZip(option.url, destZip)
                                if (result.isSuccess) {
                                    com.example.engine.fs.LocalFileManager.unzipFile(destZip, destFolder)
                                    destZip.delete()
                                    val msg = ChatMessage(text = "Successfully imported GitHub repo '$repoName' into workspace.", role = MessageRole.APP_ACTION)
                                    chatMessages.add(msg)
                                    saveMessage(msg)
                                } else {
                                    val msg = ChatMessage(text = "Failed to import repo: ${result.exceptionOrNull()?.message}", role = MessageRole.APP_ACTION)
                                    chatMessages.add(msg)
                                    saveMessage(msg)
                                }
                            }
                        }
                        is AttachmentOption.Workspace -> {
                            val msg = ChatMessage(text = "Workspace artifacts picker triggered", role = MessageRole.USER)
                            chatMessages.add(msg)
                            saveMessage(msg)
                        }
                        is AttachmentOption.GoogleDrive -> {
                            val msg = ChatMessage(text = "Google Drive picker triggered", role = MessageRole.USER)
                            chatMessages.add(msg)
                            saveMessage(msg)
                        }
                    }
                    showAttachmentPicker = false
                }
            )
        }
    }
}

@Composable
fun UserMessage(text: String, isLastMessage: Boolean = true) {
    var expanded by remember(isLastMessage) { mutableStateOf(isLastMessage) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp, end = 8.dp),
        horizontalAlignment = Alignment.End
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { expanded = !expanded }.padding(4.dp)
        ) {
            Text("You", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (expanded) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                SelectionContainer {
                    Text(
                        text = text,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun AiMessage(message: ChatMessage, isLastMessage: Boolean = true, aiViewModel: AiManagerViewModel) {
    val context = LocalContext.current
    var expanded by remember(isLastMessage) { mutableStateOf(isLastMessage) }
    var userRating by remember(message.id) { mutableStateOf<Boolean?>(null) }
    val scope = rememberCoroutineScope()
    
    val displayName = message.modelName ?: "Gemini Pro Latest"
    
    Column(modifier = Modifier.fillMaxWidth().padding(end = 32.dp, start = 8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { expanded = !expanded }.padding(4.dp)
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(displayName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        
        if (expanded) {
            Spacer(modifier = Modifier.height(8.dp))
            SelectionContainer {
                Text(text = message.text, style = MaterialTheme.typography.bodyLarge)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Copy Action
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("AI Response", message.text))
                        Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
                
                // Ratings
                if (message.modelName != null && message.providerId != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            if (userRating != true) {
                                userRating = true
                                aiViewModel.rateModel(message.providerId, message.modelName, true, message.id)
                                Toast.makeText(context, "Rated: Upvote", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(Icons.Default.ThumbUp, contentDescription = "Upvote", modifier = Modifier.size(16.dp), tint = if (userRating == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            if (userRating != false) {
                                userRating = false
                                aiViewModel.rateModel(message.providerId, message.modelName, false, message.id)
                                Toast.makeText(context, "Rated: Downvote", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(Icons.Default.ThumbDown, contentDescription = "Downvote", modifier = Modifier.size(16.dp), tint = if (userRating == false) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
@Composable
fun AppActionMessage(
    editedFiles: List<Pair<String, Boolean>> = emptyList(),
    appActions: List<String> = emptyList(),
    onFileClick: (String) -> Unit = {},
    isLastMessage: Boolean = true
) {
    Box(modifier = Modifier.fillMaxWidth().padding(end = 32.dp, start = 8.dp)) {
        ActionHistoryCard(
            editedFiles = editedFiles,
            appActions = appActions,
            onFileClick = onFileClick,
            isLastMessage = isLastMessage
        )
    }
}

@Composable
fun ActionHistoryCard(
    modifier: Modifier = Modifier,
    editedFiles: List<Pair<String, Boolean>> = emptyList(),
    appActions: List<String> = emptyList(),
    onFileClick: (String) -> Unit = {},
    isLastMessage: Boolean = true
) {
    var expanded by remember(isLastMessage) { mutableStateOf(isLastMessage && (appActions.isNotEmpty() || editedFiles.isNotEmpty())) }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = "Action history",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "App Action Log (${appActions.size + editedFiles.size})",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                
                if (appActions.isNotEmpty()) {
                    appActions.forEach { action ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Box(modifier = Modifier.size(4.dp).background(MaterialTheme.colorScheme.onSurfaceVariant, RoundedCornerShape(2.dp)))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = action,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                if (editedFiles.isNotEmpty()) {
                    // Edit section header
                    Text("Files edited:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
                    
                    editedFiles.forEach { (filePath, success) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp)
                                .clickable { onFileClick(filePath) }
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = if (success) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = if (success) "Success" else "Failed",
                                tint = if (success) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = filePath.substringAfterLast("/"),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TokenUsageBar(usedTokens: Int, maxTokens: Int) {
    val progress = usedTokens.toFloat() / maxTokens.toFloat()
    val isWarning = progress > 0.8f
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.weight(1f).height(4.dp),
            color = if (isWarning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$usedTokens / $maxTokens Tokens",
            style = MaterialTheme.typography.labelSmall,
            color = if (isWarning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

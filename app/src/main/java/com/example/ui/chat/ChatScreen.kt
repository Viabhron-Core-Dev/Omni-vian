package com.example.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.graphics.Color
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
    val editedFiles: List<Pair<String, Boolean>> = emptyList(),
    val appActions: List<String> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    sessionId: String,
    onMenuClick: () -> Unit
) {
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
                    MessageRole.AI -> AiMessage(text = message.text, isLastMessage = isLastMessage)
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
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.clickable { /* open model picker */ }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Gemini Pro", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Model", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface)
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
                                    
                                    val generatingMessage = ChatMessage(text = "Thinking...", role = MessageRole.AI)
                                    chatMessages.add(generatingMessage)
                                    
                                    isGenerating = true
                                    currentJob = scope.launch {
                                        try {
                                            val response = com.example.ui.chat.OmniRouteClient.generateContent(chatMessages.filter { it.id != generatingMessage.id })
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
                                                val msg = ChatMessage(text = response.text, role = MessageRole.AI)
                                                chatMessages.add(msg)
                                                saveMessage(msg)
                                            }
                                        } catch (e: kotlinx.coroutines.CancellationException) {
                                            val index = chatMessages.indexOf(generatingMessage)
                                            if (index != -1) {
                                                val msg = generatingMessage.copy(text = "Generation stopped.")
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
fun AiMessage(text: String, isLastMessage: Boolean = true) {
    val context = LocalContext.current
    var expanded by remember(isLastMessage) { mutableStateOf(isLastMessage) }
    Column(modifier = Modifier.fillMaxWidth().padding(end = 32.dp, start = 8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { expanded = !expanded }.padding(4.dp)
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Gemini Pro Latest", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        
        if (expanded) {
            Spacer(modifier = Modifier.height(8.dp))
            SelectionContainer {
                Text(text = text, style = MaterialTheme.typography.bodyLarge)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Restore,
                    contentDescription = "Revert",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp).clickable { 
                        Toast.makeText(context, "Workspace state reverted.", Toast.LENGTH_SHORT).show()
                    }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp).clickable { 
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("AI Message", text))
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    }
                )
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

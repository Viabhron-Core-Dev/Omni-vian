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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
    onMenuClick: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isServerRunning by PreviewServerManager.isRunning.collectAsState()

    var showAgentSettings by remember { mutableStateOf(false) }
    var showAttachmentPicker by remember { mutableStateOf(false) }
    var selectedFile by remember { mutableStateOf<String?>(null) }
    var showArtifactsList by remember { mutableStateOf(false) }
    
    val chatMessages = remember {
        mutableStateListOf(
            ChatMessage(
                text = "github/workflows/build.yml\nShow file in chat reply. Full file in codeblock. just discuss no coding or building or updating blueprint.",
                role = MessageRole.USER
            ),
            ChatMessage(
                role = MessageRole.APP_ACTION,
                appActions = listOf(
                    "Searched Workspace for 'build.yml'",
                    "Read github/workflows/build.yml",
                    "Checked Gradle configuration"
                ),
                editedFiles = listOf(
                    "BLUEPRINT.md" to true,
                    "app/src/main/java/com/example/engine/tool..." to true,
                    "app/src/main/java/com/example/engine/setti..." to true,
                    "app/src/main/java/com/example/engine/setti..." to true
                )
            ),
            ChatMessage(
                text = "I've reviewed the file. The workflow uses setup-gradle@v4 which is correct. I'll make the updates you requested.",
                role = MessageRole.AI
            )
        )
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
                    Text("Untitled", style = MaterialTheme.typography.titleMedium)
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
                        text = { Text("Rename Chat") },
                        onClick = { showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Archive Chat") },
                        onClick = { showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete Chat", color = MaterialTheme.colorScheme.error) },
                        onClick = { showMenu = false }
                    )
                }
            }
        )
        
        TokenUsageBar(usedTokens = 45000, maxTokens = 128000)
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(chatMessages, key = { it.id }) { message ->
                when (message.role) {
                    MessageRole.USER -> UserMessage(text = message.text)
                    MessageRole.AI -> AiMessage(text = message.text)
                    MessageRole.APP_ACTION -> AppActionMessage(
                        editedFiles = message.editedFiles,
                        appActions = message.appActions,
                        onFileClick = { selectedFile = it }
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
                                if (inputText.isNotBlank()) {
                                    chatMessages.add(ChatMessage(text = inputText, role = MessageRole.USER))
                                    inputText = ""
                                }
                            },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = "Send")
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
                            chatMessages.add(ChatMessage(text = "Selected image: ${option.uri}", role = MessageRole.USER))
                        }
                        is AttachmentOption.FileUri -> {
                            chatMessages.add(ChatMessage(text = "Selected file: ${option.uri}", role = MessageRole.USER))
                            scope.launch {
                                val result = com.example.engine.fs.TextExtractor.extractTextFromUri(context, option.uri)
                                if (result.isSuccess) {
                                    val text = result.getOrNull()
                                    chatMessages.add(ChatMessage(text = "File content extracted (${text?.length} chars)", role = MessageRole.APP_ACTION))
                                } else {
                                    chatMessages.add(ChatMessage(text = "Failed to extract text", role = MessageRole.APP_ACTION))
                                }
                            }
                        }
                        is AttachmentOption.GithubRepo -> {
                            chatMessages.add(ChatMessage(text = "Importing repo: ${option.url} ...", role = MessageRole.USER))
                            scope.launch {
                                val repoName = option.url.trim().removeSuffix("/").substringAfterLast("/")
                                val destFolder = java.io.File(com.example.engine.fs.LocalFileManager.getWorkspaceDir(), repoName)
                                val destZip = java.io.File(com.example.engine.fs.LocalFileManager.getWorkspaceDir(), "$repoName.zip")
                                val result = com.example.engine.fs.GithubDownloader.downloadRepoAsZip(option.url, destZip)
                                if (result.isSuccess) {
                                    com.example.engine.fs.LocalFileManager.unzipFile(destZip, destFolder)
                                    destZip.delete()
                                    chatMessages.add(ChatMessage(text = "Successfully imported GitHub repo '$repoName' into workspace.", role = MessageRole.APP_ACTION))
                                } else {
                                    chatMessages.add(ChatMessage(text = "Failed to import repo: ${result.exceptionOrNull()?.message}", role = MessageRole.APP_ACTION))
                                }
                            }
                        }
                        is AttachmentOption.Workspace -> {
                            chatMessages.add(ChatMessage(text = "Workspace artifacts picker triggered", role = MessageRole.USER))
                        }
                        is AttachmentOption.GoogleDrive -> {
                            chatMessages.add(ChatMessage(text = "Google Drive picker triggered", role = MessageRole.USER))
                        }
                    }
                    showAttachmentPicker = false
                }
            )
        }
    }
}

@Composable
fun UserMessage(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp, end = 8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AiMessage(text: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(end = 32.dp, start = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Gemini Pro Latest • Ran for 10s", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
        
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Restore,
                contentDescription = "Revert",
                modifier = Modifier.size(20.dp).clickable { /* TODO: Revert */ },
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                imageVector = Icons.Default.Code,
                contentDescription = "Diff",
                modifier = Modifier.size(20.dp).clickable { /* TODO: Diff */ },
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AppActionMessage(
    editedFiles: List<Pair<String, Boolean>> = emptyList(),
    appActions: List<String> = emptyList(),
    onFileClick: (String) -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxWidth().padding(end = 32.dp, start = 8.dp)) {
        ActionHistoryCard(
            editedFiles = editedFiles,
            appActions = appActions,
            onFileClick = onFileClick
        )
    }
}

@Composable
fun ActionHistoryCard(
    modifier: Modifier = Modifier,
    editedFiles: List<Pair<String, Boolean>> = emptyList(),
    appActions: List<String> = emptyList(),
    onFileClick: (String) -> Unit = {}
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = "Action history",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "App Action Log",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Edited ${editedFiles.size} files",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Files list
            editedFiles.forEach { (fileName, isModifiedOrAdded) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onFileClick(fileName) }
                        .padding(vertical = 4.dp, horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = fileName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    
                    if (isModifiedOrAdded) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Modified",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF4CAF50) // Green
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = "Deleted",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFFF44336) // Red
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

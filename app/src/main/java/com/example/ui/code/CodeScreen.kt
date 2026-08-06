package com.example.ui.code

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.example.engine.server.PreviewServerManager
import com.example.engine.fs.FileNode
import com.example.engine.fs.FileHistoryEngine
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeScreen(onMenuClick: () -> Unit) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val isServerRunning by PreviewServerManager.isRunning.collectAsState()
    var selectedFile by remember { mutableStateOf<FileNode?>(null) }
    val editorState = rememberCodeEditorState()
    
    var showMenu by remember { mutableStateOf(false) }
    var showRevertDialog by remember { mutableStateOf(false) }
    var showFindReplaceDialog by remember { mutableStateOf(false) }
    var showGoToLineDialog by remember { mutableStateOf(false) }
    var showSyntaxCheckDialog by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    ModalDrawerSheet(modifier = Modifier.width(300.dp)) {
                        FileExplorer(
                            onFileClick = { fileNode ->
                                selectedFile = fileNode
                            }
                        )
                    }
                }
            }
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Column(modifier = Modifier.fillMaxSize()) {
                    TopAppBar(
                        title = { 
                            Column {
                                Text(selectedFile?.name ?: "Code Editor", style = MaterialTheme.typography.titleMedium)
                                if (editorState.isLiveGeneration) {
                                    Text("Live Generation View", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onMenuClick) {
                                Icon(Icons.Default.Menu, "Global Menu")
                            }
                        },
                        actions = {
                            if (selectedFile != null) {
                                IconButton(onClick = { 
                                    editorState.saveFile()
                                    Toast.makeText(context, "Saved ${selectedFile?.name}", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Default.Save, contentDescription = "Save")
                                }
                                IconButton(onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Code", editorState.content.text)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                                }
                                IconButton(onClick = {
                                    Toast.makeText(context, "Download not fully implemented in preview", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Default.Download, contentDescription = "Download")
                                }
                            }
                            
                            IconButton(onClick = {
                                if (isServerRunning) {
                                    PreviewServerManager.stop()
                                } else {
                                    PreviewServerManager.start(File(context.cacheDir, "workspace"))
                                }
                            }) {
                                Icon(
                                    if (isServerRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = if (isServerRunning) "Stop Server" else "Start Server"
                                )
                            }
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Folder, "File Tree")
                            }
                            
                            Box {
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(Icons.Default.MoreVert, "More Options")
                                }
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Toggle Line Wrap: ${if (editorState.isLineWrapEnabled) "ON" else "OFF"}") },
                                        onClick = { 
                                            editorState.isLineWrapEnabled = !editorState.isLineWrapEnabled
                                            showMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Find & Replace") },
                                        onClick = { 
                                            showFindReplaceDialog = true
                                            showMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Go to Line") },
                                        onClick = { 
                                            showGoToLineDialog = true
                                            showMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Check Syntax Error") },
                                        onClick = { 
                                            showSyntaxCheckDialog = true
                                            showMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("File History (Revert)") },
                                        onClick = { 
                                            showRevertDialog = true
                                            showMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    )
                    
                    if (showFindReplaceDialog) {
                        FindReplaceDialog(editorState = editorState, onDismiss = { showFindReplaceDialog = false })
                    }
                    if (showGoToLineDialog) {
                        GoToLineDialog(editorState = editorState, onDismiss = { showGoToLineDialog = false })
                    }
                    if (showSyntaxCheckDialog) {
                        SyntaxCheckDialog(editorState = editorState, onDismiss = { showSyntaxCheckDialog = false })
                    }

                    if (showRevertDialog && selectedFile != null) {
                        FileRevertDialog(
                            file = selectedFile!!.file,
                            onDismiss = { showRevertDialog = false },
                            onRevert = { revisionFile ->
                                FileHistoryEngine.revertToFile(selectedFile!!.file, revisionFile)
                                scope.launch {
                                    editorState.loadFile(selectedFile!!.file) // Reload after revert
                                }
                                showRevertDialog = false
                                Toast.makeText(context, "Reverted to ${revisionFile.name}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                    
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        selectedFile?.let { fileNode ->
                            val name = fileNode.name.lowercase()
                            when {
                                name.endsWith(".pdf") -> PdfViewer(fileNode.file)
                                name.endsWith(".ppt") || name.endsWith(".pptx") -> PptViewer(fileNode.file)
                                name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".jpeg") -> {
                                    Text("Image Viewer not implemented yet", modifier = Modifier.padding(16.dp))
                                }
                                else -> TextViewer(fileNode.file, editorState)
                            }
                        } ?: run {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Select a file to view", style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FileRevertDialog(file: File, onDismiss: () -> Unit, onRevert: (File) -> Unit) {
    val revisions = remember(file) { FileHistoryEngine.getRevisions(file) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("File History & Revert") },
        text = {
            if (revisions.isEmpty()) {
                Text("No history available for this file.")
            } else {
                LazyColumn {
                    items(revisions) { revision ->
                        ListItem(
                            headlineContent = { Text(revision.name) },
                            supportingContent = { Text("Size: ${revision.length()} bytes") },
                            trailingContent = {
                                TextButton(onClick = { onRevert(revision) }) {
                                    Text("Revert")
                                }
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

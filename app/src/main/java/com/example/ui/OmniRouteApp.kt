package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.bottomnav.AppTab
import com.example.ui.bottomnav.FixedBottomNav
import com.example.ui.bottomnav.WorkspaceActionsBottomSheet
import com.example.ui.export.GithubExportBottomSheet
import com.example.ui.chat.ChatScreen
import com.example.ui.code.CodeScreen
import com.example.ui.sidebar.GlobalSidebar
import com.example.ui.settings.GlobalSettingsScreen
import com.example.ui.settings.ThreadSettingsScreen
import com.example.ui.settings.LogKeeperScreen
import com.example.utils.LogKeeper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OmniRouteApp() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentTab by remember { mutableStateOf(AppTab.CHAT) }
    var showWorkspaceActions by remember { mutableStateOf(false) }
    var showGithubExport by remember { mutableStateOf(false) }
    var chatSessionId by remember { mutableStateOf(java.util.UUID.randomUUID().toString()) }
    val context = LocalContext.current
    
    val navController = rememberNavController()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            GlobalSidebar(
                onClose = { scope.launch { drawerState.close() } },
                onNewChat = { chatSessionId = java.util.UUID.randomUUID().toString() },
                onNavigateToSettings = { 
                    scope.launch { drawerState.close() }
                    navController.navigate("settings")
                },
                currentChatId = chatSessionId,
                onChatSelected = { newSessionId -> chatSessionId = newSessionId }
            )
        }
    ) {
        NavHost(navController = navController, startDestination = "main") {
            composable("main") {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        FixedBottomNav(
                            currentTab = currentTab,
                            onTabSelected = { currentTab = it },
                            onMoreClick = { showWorkspaceActions = true }
                        )
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                        when (currentTab) {
                            AppTab.CHAT -> key(chatSessionId) {
                                remember(chatSessionId) {
                                    com.example.engine.fs.LocalFileManager.switchWorkspace(chatSessionId)
                                    true
                                }
                                ChatScreen(
                                    onMenuClick = { scope.launch { drawerState.open() } }
                                )
                            }
                            AppTab.CODE -> CodeScreen(
                                onMenuClick = { scope.launch { drawerState.open() } }
                            )
                        }
                        
                        FloatingActionButton(
                            onClick = { navController.navigate("log_keeper") },
                            modifier = Modifier
                                .align(androidx.compose.ui.Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Icon(Icons.Default.BugReport, contentDescription = "Export Logs")
                        }
                    }
                }
                
                if (showWorkspaceActions) {
                    WorkspaceActionsBottomSheet(
                        onDismiss = { showWorkspaceActions = false },
                        onExportClick = {
                            showWorkspaceActions = false
                            showGithubExport = true
                        },
                        onZipExportClick = {
                            showWorkspaceActions = false
                            scope.launch {
                                val context = navController.context
                                val dir = com.example.engine.fs.LocalFileManager.getWorkspaceDir()
                                val cacheDir = context.cacheDir
                                val zipFile = java.io.File(cacheDir, "workspace_${dir.name}.zip")
                                val result = com.example.engine.fs.LocalFileManager.zipDirectory(dir, zipFile)
                                if (result.isSuccess) {
                                    val uri = androidx.core.content.FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        zipFile
                                    )
                                    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                        type = "application/zip"
                                        putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(android.content.Intent.createChooser(intent, "Export Workspace"))
                                } else {
                                    android.widget.Toast.makeText(context, "Failed to create ZIP", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onThreadSettingsClick = {
                            showWorkspaceActions = false
                            navController.navigate("thread_settings")
                        }
                    )
                }
                if (showGithubExport) {
                    GithubExportBottomSheet(
                        onDismiss = { showGithubExport = false }
                    )
                }
            }
            
            composable("thread_settings") {
                ThreadSettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("log_keeper") {
                LogKeeperScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("settings") {
                GlobalSettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateTo = { route -> 
                        navController.navigate(route) 
                        // Note: actual nested routes for settings aren't fully implemented in this phase
                    }
                )
            }
            composable("settings/{subRoute}") { backStackEntry ->
                val subRoute = backStackEntry.arguments?.getString("subRoute") ?: "Unknown"
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(subRoute.replaceFirstChar { it.uppercase() }) },
                            navigationIcon = {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            }
                        )
                    }
                ) { padding ->
                    Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text("Settings content for $subRoute (Pending implementation)")
                    }
                }
            }
        }
    }
}

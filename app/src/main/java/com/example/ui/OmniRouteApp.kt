package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
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
import com.example.utils.LogKeeper
import kotlinx.coroutines.launch

@Composable
fun OmniRouteApp() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentTab by remember { mutableStateOf(AppTab.CHAT) }
    var showWorkspaceActions by remember { mutableStateOf(false) }
    var showGithubExport by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    val navController = rememberNavController()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            GlobalSidebar(
                onClose = { scope.launch { drawerState.close() } },
                onNavigateToSettings = { 
                    scope.launch { drawerState.close() }
                    navController.navigate("settings")
                }
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
                            AppTab.CHAT -> ChatScreen(
                                onMenuClick = { scope.launch { drawerState.open() } }
                            )
                            AppTab.CODE -> CodeScreen(
                                onMenuClick = { scope.launch { drawerState.open() } }
                            )
                        }
                        
                        FloatingActionButton(
                            onClick = { LogKeeper.exportAndClear(context) },
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

            composable("settings") {
                GlobalSettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateTo = { route -> 
                        // navController.navigate(route) 
                        // Note: actual nested routes for settings aren't fully implemented in this phase
                    }
                )
            }
        }
    }
}

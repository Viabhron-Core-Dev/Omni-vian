package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ui.bottomnav.AppTab
import com.example.ui.bottomnav.FixedBottomNav
import com.example.ui.chat.ChatScreen
import com.example.ui.code.CodeScreen
import com.example.ui.sidebar.GlobalSidebar
import com.example.utils.LogKeeper
import kotlinx.coroutines.launch

@Composable
fun OmniRouteApp() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentTab by remember { mutableStateOf(AppTab.CHAT) }
    val context = LocalContext.current

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            GlobalSidebar(
                onClose = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                FixedBottomNav(
                    currentTab = currentTab,
                    onTabSelected = { currentTab = it },
                    onMoreClick = { /* Workspace Actions */ }
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
                
                // FAB placed at the bottom-left corner
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
    }
}

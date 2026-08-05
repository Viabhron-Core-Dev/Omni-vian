package com.example.ui.code

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeScreen(onMenuClick: () -> Unit) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // We use LocalLayoutDirection to put the drawer on the right side
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    ModalDrawerSheet(modifier = Modifier.width(300.dp)) {
                        Text("File Tree", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp))
                        Divider()
                        // Dummy file tree
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Folder, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("src/main/java")
                        }
                    }
                }
            }
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Column(modifier = Modifier.fillMaxSize()) {
                    TopAppBar(
                        title = { Text("Code Editor") },
                        navigationIcon = {
                            IconButton(onClick = onMenuClick) {
                                Icon(Icons.Default.Menu, "Global Menu")
                            }
                        },
                        actions = {
                            IconButton(onClick = { 
                                scope.launch {
                                    drawerState.open()
                                }
                            }) {
                                Icon(Icons.Default.Folder, "File Tree")
                            }
                        }
                    )
                    
                    Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp)) {
                        Text("Code Editor Area", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}

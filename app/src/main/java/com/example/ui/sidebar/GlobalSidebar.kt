package com.example.ui.sidebar

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DesignServices
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R

@Composable
fun GlobalSidebar(onClose: () -> Unit, onNavigateToSettings: () -> Unit = {}) {
    ModalDrawerSheet(modifier = Modifier.width(300.dp)) {
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))
        HorizontalDivider()
        
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            label = { Text("New Chat") },
            selected = false,
            onClick = { /* TODO: New Chat */ onClose() },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
        
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Code, contentDescription = null) },
            label = { Text("Artifacts") },
            selected = false,
            onClick = { /* TODO: Artifacts */ onClose() },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
        
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.DesignServices, contentDescription = null) },
            label = { Text("Design") },
            selected = false,
            onClick = { /* TODO: Design Studio */ onClose() },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
        
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Cloud, contentDescription = null) },
            label = { Text("Library") },
            selected = false,
            onClick = { /* TODO: Library */ onClose() },

            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        
        Text("List of Chats (Repos)", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Folder, contentDescription = null) },
            label = { Text("Current Thread") },
            selected = true,
            onClick = onClose,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        
        Spacer(Modifier.weight(1f))
        HorizontalDivider()
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text("Global Settings") },
            selected = false,
            onClick = onNavigateToSettings,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

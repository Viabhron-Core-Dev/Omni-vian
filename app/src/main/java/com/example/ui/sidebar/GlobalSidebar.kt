package com.example.ui.sidebar

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GlobalSidebar(onClose: () -> Unit) {
    ModalDrawerSheet(modifier = Modifier.width(300.dp)) {
        Spacer(Modifier.height(16.dp))
        Text("OmniRoute IDE", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))
        Divider()
        
        Text("Workspaces", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp))
        NavigationDrawerItem(
            label = { Text("Current Thread") },
            selected = true,
            onClick = onClose,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        
        Spacer(Modifier.weight(1f))
        Divider()
        NavigationDrawerItem(
            label = { Text("Global Settings") },
            selected = false,
            onClick = { /* TODO: Global Settings Library */ },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

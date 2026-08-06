package com.example.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentSettingsBottomSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState()
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                "Agent Settings",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
            
            ListItem(
                headlineContent = { Text("Model Order") },
                supportingContent = { Text("Configure primary and fallback models") },
                leadingContent = { Icon(Icons.Default.List, contentDescription = null) }
            )
            
            ListItem(
                headlineContent = { Text("System Prompts") },
                supportingContent = { Text("Edit instructions and behavioral constraints") },
                leadingContent = { Icon(Icons.Default.SettingsSuggest, contentDescription = null) }
            )
            
            ListItem(
                headlineContent = { Text("Schedules & Reactions") },
                supportingContent = { Text("Manage cron jobs and event-driven hooks") },
                leadingContent = { Icon(Icons.Default.Schedule, contentDescription = null) }
            )
        }
    }
}

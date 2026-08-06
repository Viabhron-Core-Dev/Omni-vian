package com.example.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class ThreadSettingTab(val title: String) {
    UNIVERSAL("Universal"),
    AGENTS("Agents"),
    VERSIONS("Versions"),
    SECRETS("Secrets"),
    GITHUB("GitHub")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreadSettingsScreen(
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(ThreadSettingTab.UNIVERSAL) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thread Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Pill-shaped tabs
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ThreadSettingTab.values()) { tab ->
                    FilterChip(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        label = { Text(tab.title) }
                    )
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(top = 8.dp))

            // Content
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                when (selectedTab) {
                    ThreadSettingTab.UNIVERSAL -> Text("Universal Settings Content (e.g. rename thread, system instructions)")
                    ThreadSettingTab.AGENTS -> Text("Agents Content (e.g. attach/detach specific agents)")
                    ThreadSettingTab.VERSIONS -> Text("Versions Content (e.g. view snapshots)")
                    ThreadSettingTab.SECRETS -> Text("Secrets Content (e.g. thread-specific API keys)")
                    ThreadSettingTab.GITHUB -> Text("GitHub Content (e.g. branch info, sync status)")
                }
            }
        }
    }
}

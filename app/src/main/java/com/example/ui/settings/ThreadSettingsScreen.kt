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
                    ThreadSettingTab.UNIVERSAL -> UniversalSettingsContent()
                    ThreadSettingTab.AGENTS -> AgentsSettingsContent()
                    ThreadSettingTab.VERSIONS -> VersionsSettingsContent()
                    ThreadSettingTab.GITHUB -> GithubSettingsContent()
                }
            }
        }
    }
}


@Composable
fun UniversalSettingsContent() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = "Untitled Thread",
            onValueChange = {},
            label = { Text("Thread Name") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = "You are a helpful coding assistant.",
            onValueChange = {},
            label = { Text("System Instructions") },
            modifier = Modifier.fillMaxWidth().height(150.dp),
            maxLines = 5
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Enable auto-linting", modifier = Modifier.weight(1f))
            Switch(checked = true, onCheckedChange = {})
        }
    }
}

@Composable
fun AgentsSettingsContent() {
    val context = androidx.compose.ui.platform.LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Active Agents in this Thread", style = MaterialTheme.typography.titleSmall)
        ListItem(
            headlineContent = { Text("OmniRoute (Default)") },
            supportingContent = { Text("Main coding assistant") },
            trailingContent = { Switch(checked = true, onCheckedChange = {}) }
        )
        ListItem(
            headlineContent = { Text("UI Designer") },
            supportingContent = { Text("Creates UI Maps") },
            trailingContent = { Switch(checked = false, onCheckedChange = {}) }
        )
        Button(onClick = { android.widget.Toast.makeText(context, "Adding agents requires plugin integration", android.widget.Toast.LENGTH_SHORT).show() }, modifier = Modifier.fillMaxWidth()) {
            Text("Add Agent to Thread")
        }
    }
}

@Composable
fun VersionsSettingsContent() {
    val context = androidx.compose.ui.platform.LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Workspace Snapshots", style = MaterialTheme.typography.titleSmall)
        ListItem(
            headlineContent = { Text("v1.0.2 - Just now") },
            supportingContent = { Text("Auto-saved after Chat Action") },
            trailingContent = { TextButton(onClick = {}) { Text("Restore") } }
        )
        ListItem(
            headlineContent = { Text("v1.0.1 - 2 hours ago") },
            supportingContent = { Text("Manual Snapshot") },
            trailingContent = { TextButton(onClick = {}) { Text("Restore") } }
        )
        Button(onClick = { android.widget.Toast.makeText(context, "Snapshot created", android.widget.Toast.LENGTH_SHORT).show() }, modifier = Modifier.fillMaxWidth()) {
            Text("Create Snapshot")
        }
    }
}


@Composable
fun GithubSettingsContent() {
    val context = androidx.compose.ui.platform.LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Repository Connection", style = MaterialTheme.typography.titleSmall)
        OutlinedTextField(
            value = "Viabhron-Core-Dev/Omni-vian",
            onValueChange = {},
            label = { Text("Repository (owner/repo)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = "main",
            onValueChange = {},
            label = { Text("Branch") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Auto-sync on push", modifier = Modifier.weight(1f))
            Switch(checked = false, onCheckedChange = {})
        }
        Button(onClick = { android.widget.Toast.makeText(context, "GitHub connection updated", android.widget.Toast.LENGTH_SHORT).show() }, modifier = Modifier.fillMaxWidth()) {
            Text("Update Connection")
        }
    }
}

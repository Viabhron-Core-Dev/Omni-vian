package com.example.ui.settings.omniroute

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Business
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.engine.db.ApiProviderEntity
import com.example.engine.db.ApiKeyEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiManagerPanelScreen(
    onNavigateBack: () -> Unit,
    onAddKeyClick: (String) -> Unit,
    viewModel: AiManagerViewModel = viewModel()
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Directory", "Active Keys", "Available Models", "Metrics", "Model Rater", "Translator")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("OmniRoute AI Manager") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 8.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (selectedTabIndex) {
                    0 -> DirectoryTab(viewModel, onAddKeyClick)
                    1 -> ActiveKeysTab(viewModel)
                    2 -> CenterTextTab("Aggregated Models List (Pending Phase 9.3)")
                    3 -> MetricsTab(viewModel)
                    4 -> CenterTextTab("Model Ratings & Leaderboard (Pending Phase 9.12)")
                    5 -> CenterTextTab("Translator Playground Debug UI (Pending Phase 9.6)")
                }
            }
        }
    }
}

@Composable
fun CenterTextTab(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
        Text(text)
    }
}

@Composable
fun DirectoryTab(viewModel: AiManagerViewModel, onAddKeyClick: (String) -> Unit) {
    val providers by viewModel.providers.collectAsState()
    
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
        items(providers) { provider ->
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                ListItem(
                    headlineContent = { Text(provider.name) },
                    supportingContent = { Text(provider.description) },
                    leadingContent = { Icon(Icons.Default.Business, contentDescription = null) },
                    trailingContent = {
                        IconButton(onClick = { onAddKeyClick(provider.id) }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Key")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ActiveKeysTab(viewModel: AiManagerViewModel) {
    val keys by viewModel.activeKeys.collectAsState()
    val providers by viewModel.providers.collectAsState()
    
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
        if (keys.isEmpty()) {
            item { CenterTextTab("No active keys configured.") }
        } else {
            items(keys) { key ->
                val provider = providers.find { it.id == key.providerId }?.name ?: key.providerId
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    ListItem(
                        headlineContent = { Text("${key.alias} ($provider)") },
                        supportingContent = { Text(key.keyMasked) },
                        leadingContent = { Icon(Icons.Default.Key, contentDescription = null) }
                    )
                }
            }
        }
    }
}

@Composable
fun MetricsTab(viewModel: AiManagerViewModel) {
    val tokens by viewModel.totalTokens.collectAsState()
    val requests by viewModel.totalRequests.collectAsState()
    val cost by viewModel.totalCost.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            ListItem(
                headlineContent = { Text("Total Tokens Used") },
                trailingContent = { Text((tokens ?: 0).toString()) }
            )
        }
        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            ListItem(
                headlineContent = { Text("Total Requests") },
                trailingContent = { Text(requests.toString()) }
            )
        }
        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            ListItem(
                headlineContent = { Text("Estimated Cost") },
                trailingContent = { Text(String.format("$%.4f", cost ?: 0.0)) }
            )
        }
    }
}

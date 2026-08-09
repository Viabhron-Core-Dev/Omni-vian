package com.example.ui.settings.omniroute

import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectToKeyWebViewScreen(
    providerId: String,
    onNavigateBack: () -> Unit,
    viewModel: AiManagerViewModel = viewModel()
) {
    val providers by viewModel.providers.collectAsState()
    val provider = providers.find { it.id == providerId }

    var pastedKey by remember { mutableStateOf("") }
    var alias by remember { mutableStateOf("") }
    var showSaveDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Get Key: ${provider?.name ?: "Unknown"}") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = { showSaveDialog = true },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Paste & Save Key")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (provider == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("Loading provider details...")
                }
            } else {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        WebView(context).apply {
                            settings.apply {
                                javaScriptEnabled = true // Required for login pages
                                domStorageEnabled = true
                                mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                                // Safe Browsing is enabled by default on Android O+
                            }
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): Boolean {
                                    return false // Load in WebView
                                }
                            }
                            loadUrl(provider.loginUrl.takeIf { it.isNotEmpty() } ?: provider.baseUrl)
                        }
                    },
                    update = { view ->
                        // Only update if URL changes, but we assume it's static for this session
                    }
                )
            }
        }

        if (showSaveDialog) {
            AlertDialog(
                onDismissRequest = { showSaveDialog = false },
                title = { Text("Save API Key") },
                text = {
                    Column {
                        Text("Paste the API key you generated for ${provider?.name}.")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = alias,
                            onValueChange = { alias = it },
                            label = { Text("Alias (e.g., Personal, Work)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = pastedKey,
                            onValueChange = { pastedKey = it },
                            label = { Text("API Key") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (pastedKey.isNotBlank()) {
                                viewModel.saveRealKey(
                                    providerId = providerId,
                                    alias = alias.ifBlank { "${provider?.name} Key" },
                                    keyValue = pastedKey
                                )
                                showSaveDialog = false
                                onNavigateBack()
                            }
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSaveDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

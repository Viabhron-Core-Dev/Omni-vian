import re

with open('app/src/main/java/com/example/ui/settings/omniroute/AiManagerPanelScreen.kt', 'r') as f:
    content = f.read()

# Add imports
imports = """
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.animation.AnimatedVisibility
"""

# Replace models tab
replacement = """@Composable
fun ModelsTab(viewModel: AiManagerViewModel) {
    val models by viewModel.availableModels.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var expandedFolders by remember { mutableStateOf(setOf<String>()) }
    
    // Group models by provider
    val groupedModels = remember(models, searchQuery) {
        val map = mutableMapOf<String, MutableList<String>>()
        var hasValidModels = false
        
        models.forEach { modelStr ->
            if (modelStr.contains("/")) {
                hasValidModels = true
                val parts = modelStr.split("/", limit = 2)
                val provider = parts[0]
                val modelName = parts[1]
                
                if (searchQuery.isBlank() || modelName.contains(searchQuery, ignoreCase = true) || provider.contains(searchQuery, ignoreCase = true)) {
                    if (!map.containsKey(provider)) {
                        map[provider] = mutableListOf()
                    }
                    map[provider]?.add(modelName)
                }
            }
        }
        
        if (!hasValidModels) {
            // Probably says "Loading..." or "No models fetched"
            map["Status"] = models.toMutableList()
        }
        
        map
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Available Models", style = MaterialTheme.typography.titleMedium)
            Button(onClick = { viewModel.refreshModels() }) {
                Text("Refresh")
            }
        }
        
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { 
                searchQuery = it 
                // Auto-expand all if searching
                if (it.isNotBlank()) {
                    expandedFolders = groupedModels.keys.toSet()
                }
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Search models or providers...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            groupedModels.forEach { (provider, modelList) ->
                item(key = "header_$provider") {
                    val isExpanded = expandedFolders.contains(provider)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            expandedFolders = if (isExpanded) {
                                expandedFolders - provider
                            } else {
                                expandedFolders + provider
                            }
                        },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight, 
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(provider.uppercase(), style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.weight(1f))
                            Text("${modelList.size}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                
                item {
                    AnimatedVisibility(visible = expandedFolders.contains(provider) || searchQuery.isNotBlank()) {
                        Column(modifier = Modifier.fillMaxWidth().padding(start = 24.dp, top = 4.dp, bottom = 8.dp)) {
                            modelList.forEach { modelName ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(modelName, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}"""

content = content.replace("import androidx.compose.ui.unit.dp\n", "import androidx.compose.ui.unit.dp\n" + imports)
content = re.sub(r'@Composable\nfun ModelsTab\(viewModel: AiManagerViewModel\) \{.*?^\}', replacement, content, flags=re.DOTALL | re.MULTILINE)

with open('app/src/main/java/com/example/ui/settings/omniroute/AiManagerPanelScreen.kt', 'w') as f:
    f.write(content)

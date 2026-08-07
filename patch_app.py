import re

with open('app/src/main/java/com/example/ui/OmniRouteApp.kt', 'r') as f:
    content = f.read()

# Add chatSessionId
content = content.replace(
    'var showGithubExport by remember { mutableStateOf(false) }',
    'var showGithubExport by remember { mutableStateOf(false) }\n    var chatSessionId by remember { mutableStateOf(java.util.UUID.randomUUID().toString()) }'
)

# Update ChatScreen invocation
content = content.replace(
    'AppTab.CHAT -> ChatScreen(',
    'AppTab.CHAT -> ChatScreen(\n                                key = chatSessionId,'
)

# Update GlobalSidebar invocation
content = content.replace(
    'onNavigateToSettings = {',
    'onNewChat = { chatSessionId = java.util.UUID.randomUUID().toString() },\n                onNavigateToSettings = {'
)

# Replace navigation in settings
content = content.replace(
    '// navController.navigate(route)',
    'navController.navigate(route)'
)

# Add generic route catch-all for settings/something
settings_placeholders = """            composable("settings/{subRoute}") { backStackEntry ->
                val subRoute = backStackEntry.arguments?.getString("subRoute") ?: "Unknown"
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(subRoute.replaceFirstChar { it.uppercase() }) },
                            navigationIcon = {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            }
                        )
                    }
                ) { padding ->
                    Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text("Settings content for $subRoute (Pending implementation)")
                    }
                }
            }
"""

# Insert before the last closing brace of NavHost
content = re.sub(r'(        }\n    }\n})$', r'\1', content) # Ensure we don't mess up brackets
content = content.replace('        }\n    }\n}', settings_placeholders + '        }\n    }\n}')

# Fix missing Icons import for ArrowBack
if 'import androidx.compose.material.icons.automirrored.filled.ArrowBack' not in content:
    content = content.replace('import androidx.compose.material.icons.filled.BugReport', 'import androidx.compose.material.icons.filled.BugReport\nimport androidx.compose.material.icons.automirrored.filled.ArrowBack')

with open('app/src/main/java/com/example/ui/OmniRouteApp.kt', 'w') as f:
    f.write(content)


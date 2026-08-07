with open('app/src/main/java/com/example/ui/bottomnav/WorkspaceActionsBottomSheet.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'WorkspaceActionsBottomSheet(onDismiss: () -> Unit, onExportClick: () -> Unit = {}, onThreadSettingsClick: () -> Unit = {})',
    'WorkspaceActionsBottomSheet(onDismiss: () -> Unit, onExportClick: () -> Unit = {}, onZipExportClick: () -> Unit = {}, onThreadSettingsClick: () -> Unit = {})'
)

content = content.replace(
"""                    ListItem(
                        headlineContent = { Text("Export as ZIP") },
                        leadingContent = { Icon(Icons.Default.FolderZip, null) },
                        modifier = Modifier.clickable { 
                             showExportOptions = false 
                             onDismiss() 
                         }
                    )""",
"""                    ListItem(
                        headlineContent = { Text("Export as ZIP") },
                        leadingContent = { Icon(Icons.Default.FolderZip, null) },
                        modifier = Modifier.clickable { 
                             showExportOptions = false 
                             onZipExportClick()
                             onDismiss() 
                         }
                    )"""
)

with open('app/src/main/java/com/example/ui/bottomnav/WorkspaceActionsBottomSheet.kt', 'w') as f:
    f.write(content)


import re

with open('app/src/main/java/com/example/ui/settings/GlobalSettingsScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    '                SettingsItem("OmniRoute", "Local proxy settings", Icons.Default.Router) { onNavigateTo("settings/omniroute") }',
    '''                SettingsItem("OmniRoute", "Local proxy settings", Icons.Default.Router) { onNavigateTo("settings/omniroute") }
                SettingsItem("Log Keeper", "View and export app logs", Icons.Default.BugReport) { onNavigateTo("settings/log_keeper") }'''
)

content = content.replace('import androidx.compose.material.icons.filled.*', 'import androidx.compose.material.icons.filled.*\nimport androidx.compose.material.icons.filled.BugReport')

with open('app/src/main/java/com/example/ui/settings/GlobalSettingsScreen.kt', 'w') as f:
    f.write(content)


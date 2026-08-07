with open('app/src/main/java/com/example/ui/sidebar/GlobalSidebar.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'fun GlobalSidebar(onClose: () -> Unit, onNavigateToSettings: () -> Unit = {}) {',
    'fun GlobalSidebar(onClose: () -> Unit, onNavigateToSettings: () -> Unit = {}, onNewChat: () -> Unit = {}) {'
)
content = content.replace(
    'onClick = { /* TODO: New Chat */ onClose() },',
    'onClick = { onNewChat(); onClose() },'
)

with open('app/src/main/java/com/example/ui/sidebar/GlobalSidebar.kt', 'w') as f:
    f.write(content)


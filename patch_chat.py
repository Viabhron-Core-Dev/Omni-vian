import re

with open('app/src/main/java/com/example/ui/chat/ChatScreen.kt', 'r') as f:
    content = f.read()

# Add imports
imports = """
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.material.icons.filled.ContentCopy
"""

content = content.replace('import androidx.compose.material.icons.filled.Restore', 'import androidx.compose.material.icons.filled.Restore\nimport androidx.compose.material.icons.filled.ContentCopy')
content = content.replace('import androidx.compose.ui.platform.LocalContext', 'import android.content.ClipData\nimport android.content.ClipboardManager\nimport android.content.Context\nimport android.widget.Toast\nimport androidx.compose.ui.platform.LocalContext')

# Add context and copy button
ai_message_new = """@Composable
fun AiMessage(text: String) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxWidth().padding(end = 32.dp, start = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Gemini Pro Latest • Ran for 10s", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
        
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Restore,
                contentDescription = "Revert",
                modifier = Modifier.size(20.dp).clickable { /* TODO: Revert */ },
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                imageVector = Icons.Default.Code,
                contentDescription = "Diff",
                modifier = Modifier.size(20.dp).clickable { /* TODO: Diff */ },
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy",
                modifier = Modifier.size(20.dp).clickable {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("AI Message", text))
                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                },
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}"""

# Find AiMessage function
start_idx = content.find('fun AiMessage(text: String) {')
end_idx = content.find('fun AppActionMessage(', start_idx) - 1

content = content[:start_idx-11] + ai_message_new + content[end_idx:]

with open('app/src/main/java/com/example/ui/chat/ChatScreen.kt', 'w') as f:
    f.write(content)


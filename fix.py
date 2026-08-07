import re

# Fix ChatScreen.kt
with open('app/src/main/java/com/example/ui/chat/ChatScreen.kt', 'r') as f:
    chat_content = f.read()

chat_content = chat_content.replace(
    'var showArtifactsList by remember { mutableStateOf(false) }',
    'var showArtifactsList by remember { mutableStateOf(false) }\n    var isGenerating by remember { mutableStateOf(false) }\n    var currentJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }'
)

with open('app/src/main/java/com/example/ui/chat/ChatScreen.kt', 'w') as f:
    f.write(chat_content)

# Fix GeminiClient.kt imports
with open('app/src/main/java/com/example/ui/chat/GeminiClient.kt', 'r') as f:
    gemini_content = f.read()

import_block_bad = """import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import okhttp3.Request
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException"""

import_block_good = """import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import okhttp3.Request"""

gemini_content = gemini_content.replace(import_block_bad, import_block_good)

with open('app/src/main/java/com/example/ui/chat/GeminiClient.kt', 'w') as f:
    f.write(gemini_content)


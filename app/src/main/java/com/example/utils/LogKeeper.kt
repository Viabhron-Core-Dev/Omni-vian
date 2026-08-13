package com.example.utils

import android.content.Context
import android.os.Environment
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LogEntry(
    val timestamp: Long,
    val type: String, // e.g., "ERROR", "CRASH", "FAILURE"
    val component: String,
    val message: String,
    val stackTrace: String? = null
)

object LogKeeper {
    private const val TAG = "LogKeeper"
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    private val _isEnabled = MutableStateFlow(true)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    fun toggle(enabled: Boolean) {
        _isEnabled.value = enabled
    }

    fun log(type: String, component: String, message: String, stackTrace: String? = null) {
        if (!_isEnabled.value) return

        // Filter out passwords and credentials (basic sanitization)
        val sanitizedMessage = sanitize(message)
        val sanitizedStackTrace = stackTrace?.let { sanitize(it) }

        val entry = LogEntry(
            timestamp = System.currentTimeMillis(),
            type = type,
            component = component,
            message = sanitizedMessage,
            stackTrace = sanitizedStackTrace
        )
        
        _logs.value = _logs.value + entry
        Log.e(TAG, "[\$type] \$component: \$sanitizedMessage")
    }

    fun exportAndClear(context: Context) {
        if (_logs.value.isEmpty()) return

        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (downloadsDir != null && downloadsDir.exists() || downloadsDir?.mkdirs() == true) {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val file = File(downloadsDir, "OmniRoot_Log_\$timestamp.txt")
            
            try {
                file.printWriter().use { out ->
                    out.println("--- OmniRoot Log Export ---")
                    _logs.value.forEach { entry ->
                        val timeString = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(entry.timestamp))
                        out.println("[\$timeString] [\${entry.type}] \${entry.component}")
                        out.println("Message: \${entry.message}")
                        if (entry.stackTrace != null) {
                            out.println("StackTrace:\n\${entry.stackTrace}")
                        }
                        out.println("----------------------------------------")
                    }
                }
                _logs.value = emptyList() // clear active log state
            } catch (e: Exception) {
                Log.e(TAG, "Failed to export logs", e)
            }
        }
    }

    private fun sanitize(input: String): String {
        return input.replace(Regex("(?i)(password|secret|key|token|credential)[\\\\s=:]+[^\\\\s,;]+"), "$1=***SANITIZED***")
    }
}

import re

with open('app/src/main/java/com/example/engine/omniroute/service/OmniRouteProxyServer.kt', 'r') as f:
    content = f.read()

replacement = """                val modelStr = request.model ?: "google_ai_studio/gemini-1.5-pro-latest"
                val slashIdx = modelStr.indexOf('/')
                
                val providerId = if (slashIdx != -1) modelStr.substring(0, slashIdx) else "google_ai_studio"
                val actualModelName = if (slashIdx != -1) modelStr.substring(slashIdx + 1) else modelStr

                // Determine target format and base url dynamically from provider pre-populator
                val (targetFormat, baseUrl) = when (providerId) {
                    "google_ai_studio" -> Pair(TranslationEngine.ProviderFormat.OPENAI, "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions")
                    "openai" -> Pair(TranslationEngine.ProviderFormat.OPENAI, "https://api.openai.com/v1/chat/completions")
                    "anthropic" -> Pair(TranslationEngine.ProviderFormat.ANTHROPIC, "https://api.anthropic.com/v1/messages")
                    "openrouter" -> Pair(TranslationEngine.ProviderFormat.OPENAI, "https://openrouter.ai/api/v1/chat/completions")
                    "groq" -> Pair(TranslationEngine.ProviderFormat.OPENAI, "https://api.groq.com/openai/v1/chat/completions")
                    "together_ai" -> Pair(TranslationEngine.ProviderFormat.OPENAI, "https://api.together.xyz/v1/chat/completions")
                    "local_gguf" -> Pair(TranslationEngine.ProviderFormat.OPENAI, "http://localhost:8080/v1/chat/completions")
                    else -> Pair(TranslationEngine.ProviderFormat.OPENAI, "https://api.openai.com/v1/chat/completions")
                }

                val db = AppDatabase.getDatabase(context)"""

content = re.sub(r'                val modelStr = request\.model \?\: "Gemini Pro Latest"\s+// Determine provider and format.*?val db = AppDatabase\.getDatabase\(context\)', replacement, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/engine/omniroute/service/OmniRouteProxyServer.kt', 'w') as f:
    f.write(content)

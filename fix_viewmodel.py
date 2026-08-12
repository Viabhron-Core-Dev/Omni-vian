import re

with open('app/src/main/java/com/example/ui/settings/omniroute/AiManagerViewModel.kt', 'r') as f:
    content = f.read()

replacement = """                        if (response.isSuccessful) {
                            val modelsResp = try { modelsAdapter.fromJson(responseBody) } catch(e: Exception) { null }
                            var entities = modelsResp?.data?.map { 
                                AiModelEntity(providerId = provider.id, modelId = it.id) 
                            } ?: emptyList()
                            
                            if (entities.isEmpty()) {
                                val fallbacks = when(provider.id) {
                                    "anthropic" -> listOf("claude-3-5-sonnet-20240620", "claude-3-opus-20240229")
                                    "google_ai_studio" -> listOf("gemini-1.5-pro-latest", "gemini-1.5-flash-latest")
                                    "openai" -> listOf("gpt-4o", "gpt-4o-mini")
                                    "openrouter" -> listOf("meta-llama/llama-3-8b-instruct:free")
                                    "groq" -> listOf("llama3-8b-8192")
                                    else -> emptyList()
                                }
                                entities = fallbacks.map { AiModelEntity(providerId = provider.id, modelId = it) }
                            }
                            
                            aiModelDao.deleteModelsForProvider(provider.id)
                            if (entities.isNotEmpty()) {
                                aiModelDao.insertModels(entities)
                            }
                        } else {
                            val fallbacks = when(provider.id) {
                                "anthropic" -> listOf("claude-3-5-sonnet-20240620", "claude-3-opus-20240229")
                                "google_ai_studio" -> listOf("gemini-1.5-pro-latest", "gemini-1.5-flash-latest")
                                "openai" -> listOf("gpt-4o", "gpt-4o-mini")
                                "openrouter" -> listOf("meta-llama/llama-3-8b-instruct:free")
                                "groq" -> listOf("llama3-8b-8192")
                                else -> emptyList()
                            }
                            if (fallbacks.isNotEmpty()) {
                                aiModelDao.deleteModelsForProvider(provider.id)
                                aiModelDao.insertModels(fallbacks.map { AiModelEntity(providerId = provider.id, modelId = it) })
                            }
                            Log.e("AiManager", "Failed to fetch models for ${provider.id}: ${response.code} $responseBody")
                        }"""

content = re.sub(r'                        if \(response\.isSuccessful\).*?Log\.e\("AiManager", "Failed to fetch models for \$\{provider\.id\}: \$\{response\.code\} \$responseBody"\)\n                        \}', replacement, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/settings/omniroute/AiManagerViewModel.kt', 'w') as f:
    f.write(content)

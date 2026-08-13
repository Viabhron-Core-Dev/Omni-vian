import re

with open('app/src/main/java/com/example/ui/settings/omniroute/AiManagerViewModel.kt', 'r') as f:
    content = f.read()

# Remove the incorrectly appended function
content = re.sub(r'\n    fun rateModel.*?$', '', content, flags=re.DOTALL)

# Insert the function before the last closing brace
new_func = """
    fun rateModel(providerId: String, modelName: String, isPositive: Boolean) {
        viewModelScope.launch {
            modelRatingDao.insertRating(
                com.example.engine.db.ModelRatingEntity(
                    id = java.util.UUID.randomUUID().toString(),
                    modelName = modelName,
                    providerId = providerId,
                    isPositive = isPositive,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }
}
"""

content = content.rstrip()
if content.endswith('}'):
    content = content[:-1] + new_func

with open('app/src/main/java/com/example/ui/settings/omniroute/AiManagerViewModel.kt', 'w') as f:
    f.write(content)

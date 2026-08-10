package com.example.engine.omniroute.pipeline

object CompressionEngine {
    enum class CompressionLevel {
        NONE,
        LIGHT, // Trims whitespaces, normalizes punctuation
        CAVEMAN // Removes filler words, aggressive whitespace removal
    }

    fun compress(text: String, level: CompressionLevel): String {
        if (level == CompressionLevel.NONE) return text

        var result = text.trim().replace(Regex("\\s+"), " ")

        if (level == CompressionLevel.CAVEMAN) {
            val stopWords = setOf(
                "please", "can", "you", "could", "would", "a", "an", "the", 
                "is", "are", "am", "was", "were", "to", "of", "and", "in", 
                "that", "have", "i", "it", "for", "not", "on", "with", "he", 
                "as", "do", "at", "this", "but", "his", "by", "from", 
                "they", "we", "say", "her", "she", "or", "will", "my", 
                "one", "all", "there", "their", "what", "so", "up", 
                "out", "if", "about", "who", "get", "which", "go", "me",
                "kindly", "thanks", "thank"
            )
            val words = result.split(" ").filter { it.lowercase() !in stopWords }
            result = words.joinToString(" ")
        }
        
        return result
    }
}

package com.example.ngdtechsupport.utils

object SecurityValidator {
    
    const val MAX_MESSAGE_LENGTH = 5000
    const val MAX_NAME_LENGTH = 100
    const val MAX_TITLE_LENGTH = 200
    
    fun isValidMessage(message: String?): Boolean {
        if (message.isNullOrBlank()) return false
        if (message.length > MAX_MESSAGE_LENGTH) return false
        return !containsInvalidChars(message)
    }
    
    fun isValidName(name: String?): Boolean {
        if (name.isNullOrBlank()) return false
        if (name.length > MAX_NAME_LENGTH) return false
        return !containsInvalidChars(name)
    }
    
    fun isValidTitle(title: String?): Boolean {
        if (title.isNullOrBlank()) return false
        if (title.length > MAX_TITLE_LENGTH) return false
        return !containsInvalidChars(title)
    }
    
    private fun containsInvalidChars(text: String): Boolean {
        val dangerousPatterns = listOf(
            "<script",
            "javascript:",
            "onerror=",
            "onload=",
            "eval(",
            "DROP TABLE",
            "DELETE FROM",
            "--",
            "/*",
            "*/"
        )
        
        val lowerText = text.lowercase()
        return dangerousPatterns.any { lowerText.contains(it) }
    }
    
    fun sanitizeInput(input: String): String {
        return input
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("&", "&amp;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
            .trim()
    }
}

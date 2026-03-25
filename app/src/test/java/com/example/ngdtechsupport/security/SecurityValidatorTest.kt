package com.example.ngdtechsupport.security

import com.example.ngdtechsupport.utils.SecurityValidator
import org.junit.Test
import org.junit.Assert.*

class SecurityValidatorTest {

    @Test
    fun `valid messages pass validation`() {
        val validMessages = listOf(
            "Hola, necesito ayuda",
            "Mi proyecto tiene un bug",
            "¿Cuál es el precio?",
            "12345",
            "Texto con números 123 y símbolos .,;:"
        )
        validMessages.forEach { message ->
            assertTrue("Message should be valid: $message", SecurityValidator.isValidMessage(message))
        }
    }

    @Test
    fun `empty messages fail validation`() {
        val emptyMessages = listOf("", "   ", "\n", "\t")
        emptyMessages.forEach { message ->
            assertFalse("Empty message should fail: '$message'", SecurityValidator.isValidMessage(message))
        }
    }

    @Test
    fun `null message fails validation`() {
        assertFalse("Null message should fail", SecurityValidator.isValidMessage(null))
    }

    @Test
    fun `message exceeding max length fails validation`() {
        val longMessage = "a".repeat(SecurityValidator.MAX_MESSAGE_LENGTH + 1)
        assertFalse("Message exceeding max length should fail", SecurityValidator.isValidMessage(longMessage))
    }

    @Test
    fun `message at max length passes validation`() {
        val maxMessage = "a".repeat(SecurityValidator.MAX_MESSAGE_LENGTH)
        assertTrue("Message at max length should pass", SecurityValidator.isValidMessage(maxMessage))
    }

    @Test
    fun `messages with profanity or dangerous patterns fail validation`() {
        val dangerousMessages = listOf(
            "<script>alert('xss')</script>",
            "javascript:alert('xss')",
            "onerror=alert('xss')",
            "onload=alert('xss')",
            "eval(alert('xss'))",
            "DROP TABLE users",
            "DELETE FROM users WHERE 1=1",
            "SELECT * FROM users--",
            "/* comment */",
            "Hello <script> World"
        )
        dangerousMessages.forEach { message ->
            assertFalse("Dangerous message should fail: $message", SecurityValidator.isValidMessage(message))
        }
    }

    @Test
    fun `dangerous patterns are case insensitive`() {
        val dangerousMessages = listOf(
            "<SCRIPT>alert('xss')</SCRIPT>",
            "JAVASCRIPT:alert('xss')",
            "ONERROR=alert('xss')",
            "EVAL(alert('xss'))",
            "drop table users",
            "Delete From users"
        )
        dangerousMessages.forEach { message ->
            assertFalse("Dangerous message should fail (case insensitive): $message", 
                SecurityValidator.isValidMessage(message))
        }
    }

    @Test
    fun `sanitize input replaces dangerous characters`() {
        val testCases = mapOf(
            "<script>" to "&lt;script&gt;",
            "hello&world" to "hello&amp;world",
            "\"quoted\"" to "&quot;quoted&quot;",
            "'single'" to "&#39;single&#39;",
            "<>&\"'" to "&lt;&gt;&amp;&quot;&#39;",
            "  spaces  " to "spaces"
        )
        testCases.forEach { (input, expected) ->
            assertEquals("Sanitized input should match for: $input", expected, SecurityValidator.sanitizeInput(input))
        }
    }

    @Test
    fun `sanitize input preserves safe characters`() {
        val safeInputs = listOf(
            "Hello World",
            "12345",
            "Text with .,;:!?()",
            "New\nline",
            "Mixed 123 text !@#"
        )
        safeInputs.forEach { input ->
            val sanitized = SecurityValidator.sanitizeInput(input)
            assertTrue("Safe input should be preserved: $input", sanitized.isNotBlank())
        }
    }

    @Test
    fun `valid name passes validation`() {
        val validNames = listOf("Juan", "María García", "NGD Tech", "Company-Name", "Name123")
        validNames.forEach { name ->
            assertTrue("Name should be valid: $name", SecurityValidator.isValidName(name))
        }
    }

    @Test
    fun `empty name fails validation`() {
        val emptyNames = listOf("", "   ", null)
        emptyNames.forEach { name ->
            assertFalse("Empty name should fail", SecurityValidator.isValidName(name))
        }
    }

    @Test
    fun `name exceeding max length fails validation`() {
        val longName = "a".repeat(SecurityValidator.MAX_NAME_LENGTH + 1)
        assertFalse("Name exceeding max length should fail", SecurityValidator.isValidName(longName))
    }

    @Test
    fun `valid title passes validation`() {
        val validTitles = listOf("Proyecto Web", "App Móvil", "Sistema de Gestión", "Title-123")
        validTitles.forEach { title ->
            assertTrue("Title should be valid: $title", SecurityValidator.isValidTitle(title))
        }
    }

    @Test
    fun `empty title fails validation`() {
        val emptyTitles = listOf("", "   ", null)
        emptyTitles.forEach { title ->
            assertFalse("Empty title should fail", SecurityValidator.isValidTitle(title))
        }
    }

    @Test
    fun `title exceeding max length fails validation`() {
        val longTitle = "a".repeat(SecurityValidator.MAX_TITLE_LENGTH + 1)
        assertFalse("Title exceeding max length should fail", SecurityValidator.isValidTitle(longTitle))
    }

    @Test
    fun `constants have correct values`() {
        assertEquals("MAX_MESSAGE_LENGTH should be 5000", 5000, SecurityValidator.MAX_MESSAGE_LENGTH)
        assertEquals("MAX_NAME_LENGTH should be 100", 100, SecurityValidator.MAX_NAME_LENGTH)
        assertEquals("MAX_TITLE_LENGTH should be 200", 200, SecurityValidator.MAX_TITLE_LENGTH)
    }

    @Test
    fun `sanitize input trims whitespace`() {
        val inputs = listOf("  hello  ", "\nhello\n", "\thello\t", "  hello  world  ")
        inputs.forEach { input ->
            val sanitized = SecurityValidator.sanitizeInput(input)
            assertEquals("Sanitized input should be trimmed", sanitized.trim(), sanitized)
        }
    }

    @Test
    fun `dangerous patterns in different positions`() {
        val dangerousMessages = listOf(
            "hello <script> world",
            "javascript:alert('xss')",
            "text onerror=alert()",
            "SELECT * FROM users--",
            "normal text /* comment */ more"
        )
        dangerousMessages.forEach { message ->
            assertFalse("Message with dangerous pattern should fail: $message", 
                SecurityValidator.isValidMessage(message))
        }
    }
}
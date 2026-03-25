package com.example.ngdtechsupport.ai

import org.junit.Test
import org.junit.Assert.*

class AiResponseHelperTest {

    @Test
    fun `greeting returns welcome message`() {
        val greetings = listOf("hola", "buenos días", "hi", "Hola", "HOLA")
        greetings.forEach { greeting ->
            val response = AiResponseHelper.getResponse(greeting)
            assertTrue("Response should contain welcome message for: $greeting",
                response.contains("asistente de NGD Tech Solutions") || response.contains("ayudarte"))
        }
    }

    @Test
    fun `status query returns appropriate response`() {
        val statusQueries = listOf("estado del proyecto", "progreso", "version", "versión")
        statusQueries.forEach { query ->
            val response = AiResponseHelper.getResponse(query)
            assertTrue("Response should ask for project name for: $query",
                response.contains("nombre") && response.contains("proyecto"))
        }
    }

    @Test
    fun `error report returns diagnostic prompt`() {
        val errorQueries = listOf("error en la app", "problema con login", "bug", "no funciona", "falla")
        errorQueries.forEach { query ->
            val response = AiResponseHelper.getResponse(query)
            assertTrue("Response should ask for diagnostic info for: $query",
                response.contains("función falla") || response.contains("error exacto"))
        }
    }

    @Test
    fun `quote request returns pricing information`() {
        val quoteQueries = listOf("presupuesto", "precio", "coste", "costo", "cotizacion", "cotización")
        quoteQueries.forEach { query ->
            val response = AiResponseHelper.getResponse(query)
            assertTrue("Response should ask for project details for: $query",
                response.contains("presupuesto") || response.contains("tipo de proyecto"))
        }
    }

    @Test
    fun `agent request returns transfer message`() {
        val agentQueries = listOf("agente", "hablar con humano", "persona")
        agentQueries.forEach { query ->
            val response = AiResponseHelper.getResponse(query)
            assertTrue("Response should mention agent transfer for: $query",
                response.contains("agente humano") || response.contains("atenderemos"))
        }
    }

    @Test
    fun `cleanResponse removes unwanted symbols`() {
        // Create a test instance to access cleanResponse via reflection or test indirectly
        // Since cleanResponse is private, we test through getResponse which uses it
        val testMessage = "hola"
        val response = AiResponseHelper.getResponse(testMessage)
        
        // Should not contain markdown symbols
        assertFalse("Response should not contain **", response.contains("**"))
        assertFalse("Response should not contain //", response.contains("//"))
        assertFalse("Response should not contain !!", response.contains("!!"))
        assertFalse("Response should not contain ##", response.contains("##"))
        
        // Should not have multiple punctuation
        assertFalse("Response should not have multiple !", response.contains("!!"))
        assertFalse("Response should not have multiple ?", response.contains("??"))
        assertFalse("Response should not have multiple .", response.contains(".."))
    }

    @Test
    fun `context awareness works with lastTopic`() {
        // First ask about presupuesto
        AiResponseHelper.getResponse("quiero un presupuesto")
        
        // Then ask about plazo - should follow presupuesto context
        val response = AiResponseHelper.getResponse("¿cuál es el plazo?")
        assertTrue("Response should continue presupuesto context",
            response.contains("presupuesto") || response.contains("estimación"))
    }

    @Test
    fun `context awareness resets on new topic`() {
        // Set context to presupuesto
        AiResponseHelper.getResponse("presupuesto")
        
        // Ask about greeting - should reset context
        AiResponseHelper.getResponse("hola")
        
        // Ask about plazo - should not continue presupuesto context
        val response = AiResponseHelper.getResponse("plazo")
        assertFalse("Response should not continue presupuesto context after greeting",
            response.contains("estimación"))
    }

    @Test
    fun `unknown message returns default help response`() {
        val response = AiResponseHelper.getResponse("xyz123 unknown message")
        assertTrue("Response should contain help information",
            response.contains("ayudarte") || response.contains("estado") || 
            response.contains("errores") || response.contains("presupuesto"))
    }

    @Test
    fun `empty message is handled gracefully`() {
        val response = AiResponseHelper.getResponse("")
        // Should return a response (either default or empty handling)
        assertNotNull("Response should not be null", response)
    }

    @Test
    fun `message case insensitivity`() {
        val response1 = AiResponseHelper.getResponse("HOLA")
        val response2 = AiResponseHelper.getResponse("hola")
        val response3 = AiResponseHelper.getResponse("Hola")
        
        // All should return similar welcome messages
        assertTrue("All case variations should return welcome message",
            response1.contains("asistente") && response2.contains("asistente") && response3.contains("asistente"))
    }

    @Test
    fun `response ends with punctuation`() {
        val messages = listOf("hola", "estado", "error", "presupuesto", "agente")
        messages.forEach { message ->
            val response = AiResponseHelper.getResponse(message)
            assertTrue("Response should end with punctuation for: $message",
                response.endsWith(".") || response.endsWith("!") || response.endsWith("?"))
        }
    }
}
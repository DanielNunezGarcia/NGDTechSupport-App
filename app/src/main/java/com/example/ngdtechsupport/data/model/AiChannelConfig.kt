package com.example.ngdtechsupport.data.model

data class AiChannelConfig(
    val id: String = "",
    val companyId: String = "",
    val aiEnabled: Boolean = true,
    val autoGreeting: Boolean = true,
    val greetingMessages: List<String> = defaultGreetingMessages,
    val quickReplies: List<String> = defaultQuickReplies,
    val escalationKeywords: List<String> = defaultEscalationKeywords,
    val fallbackMessage: String = defaultFallbackMessage,
    val autoTransferEnabled: Boolean = true,
    val knowledgeBaseEnabled: Boolean = true
) {
    companion object {
        val defaultGreetingMessages = listOf(
            "¡Hola! 👋 Soy el asistente de NGD Tech Solutions.",
            "Somos especialistas en desarrollo de apps móviles con IA y soporte técnico.",
            "¿En qué puedo ayudarte hoy?",
            "",
            "Puedo asistirte con:",
            "📱 Desarrollo de apps (Android/iOS)",
            "🤖 Integración de Inteligencia Artificial",
            "🐛 Soporte técnico y mantenimiento",
            "📊 Estado de tus proyectos",
            "💬 Consultas generales",
            "",
            "O escribe directamente tu duda y te ayudo.",
            "Si prefieres hablar con una persona, escribe 'hablar con agente'",
            "y te conectaré con nuestro equipo. 😊"
        )

        val defaultQuickReplies = listOf(
            "🤖 Consultar estado de mi proyecto",
            "🐛 Reportar un problema/error",
            "💰 Solicitar presupuesto",
            "❓ Pregunta general",
            "👤 Hablar con agente humano"
        )

        val defaultEscalationKeywords = listOf(
            "hablar con persona",
            "hablar con agente",
            "necesito ayuda humana",
            "no me puedes ayudar",
            "quiero hablar con alguien",
            "atención personal",
            "urgente",
            "hablar con un humano",
            "pasame con un agente"
        )

        val defaultFallbackMessage = """
            Entiendo que prefieres hablar con una persona. 😊
            
            He transferido tu conversación a nuestro equipo de soporte.
            Un agente humano revisará tu caso y te contactará pronto.
            
            Mientras tanto, ¿puedes darme más detalles sobre tu problema?
            Así podremos acelerar la atención cuando un agente te atienda.
        """.trimIndent()
    }
}

package com.example.ngdtechsupport.ai

object AiResponseHelper {
    
    const val USE_OPENAI = false
    
    fun getResponse(userMessage: String): String {
        val message = userMessage.lowercase().trim()
        
        if (USE_OPENAI) {
            return "🔧 Integrando con OpenAI..."
        }
        
        return getPredefinedResponse(message)
    }
    
    private fun getPredefinedResponse(message: String): String {
        return when {
            message.contains("hola") || message.contains("buenos") || message.contains("hi") ->
                """¡Hola! 👋 Soy el asistente de NGD Tech Solutions.
Somos especialistas en desarrollo de apps móviles con IA y soporte técnico.
¿En qué puedo ayudarte hoy?"""

            message.contains("estado") || message.contains("progreso") || message.contains("versión") || message.contains("consultar el estado") ->
                """📊 Para consultar el estado de tu proyecto:
- ¿Qué aplicación te gustaría consultar?
- O escribe el nombre de tu proyecto

Mientras tanto, ¿hay algo más en lo que pueda ayudarte?"""

            message.contains("error") || message.contains("problema") || message.contains("bug") || message.contains("no funciona") || message.contains("tengo un problema") ->
                """😟 Entiendo que tienes un problema.
Para ayudarte mejor, necesito saber:
1. ¿Qué exactamente no funciona?
2. ¿Qué mensaje de error aparece?
3. ¿Cuándo empezó a ocurrir?

Cuantos más detalles, más rápido podré ayudarte."""

            message.contains("presupuesto") || message.contains("precio") || message.contains("coste") || message.contains("cuanto") || message.contains("solicitar") ->
                """💰 Para solicitar un presupuesto, necesitamos:
1. Tipo de proyecto (app móvil, web, ambos)
2. Funcionalidades principales
3. Plazo deseado
4. Diseño propio o necesitas uno

¿Quieres que un agente comercial te contacte? Escribe "hablar con agente" """

            message.contains("servicio") || message.contains("qué haces") || message.contains("qué ofrecen") ->
                """📱 NGD Tech Solutions ofrece:

• Desarrollo de Apps (Android/iOS)
• Integración de Inteligencia Artificial
• Soporte técnico y mantenimiento
• Consultorías técnicas
• Publicación en stores

¿Sobre qué servicio necesitas más información?"""

            message.contains("quién") || message.contains("empresa") || message.contains("sobre ti") ->
                """🏢 NGD Tech Solutions

Somos una empresa especializada en desarrollo de apps móviles con IA.
Atendemos a:
• Empresas de comercio
• Hostelería
• Informática
• Logística
• Freelancers y autónomos

Nuestro objetivo es hacer tecnología accesible para todos."""

            message.contains("hablar con agente") || message.contains("persona") || message.contains("humano") || message.contains("agente humano") ->
                """👤 He transferido tu solicitud a nuestro equipo.

Un agente humano te atenderá pronto.
¿Puedes darme más detalles sobre tu consulta?
Así podremos ayudarte mejor."""

            message.contains("urgente") || message.contains("ahora") || message.contains("rápido") ->
                """⚠️ Entiendo que es urgente.

He marcado tu caso como prioritario.
Un agente humano te contactará lo antes posible.
¿Hay algún detalle adicional que debamos saber?"""

            message.contains("gracias") || message.contains("adiós") || message.contains("bye") ->
                """¡De nada! 😊

Si tienes más dudas, no dudes en escribir.
¡Un saludo de parte de NGD Tech Solutions!
🤖"""

            else ->
                """Entiendo tu mensaje, pero necesito más detalles. 😅

Puedo ayudarte con:
• Estado de tu proyecto
• Reportar errores o problemas
• Información sobre servicios
• Solicitar presupuestos

¿Qué necesitas exactamente?"""
        }
    }
}

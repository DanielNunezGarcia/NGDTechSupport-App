package com.example.ngdtechsupport.ai

object AiResponseHelper {
    
    const val USE_OPENAI = false
    
    // Contexto simple para recordar el último tema
    private var lastTopic: String = ""
    
    fun getResponse(userMessage: String): String {
        val message = userMessage.lowercase().trim()
        
        if (USE_OPENAI) {
            return cleanResponse("🔧 Integrando con OpenAI...")
        }
        
        return cleanResponse(getPredefinedResponse(message))
    }
    
    private fun cleanResponse(response: String): String {
        var clean = response
        // Remover símbolos de formato markdown
        clean = clean.replace("**", "") // negritas
        clean = clean.replace("##", "") // headings
        clean = clean.replace("~~", "") // tachado
        clean = clean.replace("```", "") // bloques de código
        
        // Remover símbolos especiales no deseados
        clean = clean.replace("!!", "!") // doble exclamación
        clean = clean.replace("¡¡", "¡") // doble exclamación de apertura
        clean = clean.replace("//", "") // doble slash
        clean = clean.replace("/", " ") // slash simple por espacio
        
        // Remover otros símbolos de formato
        clean = clean.replace("*", "") // asteriscos
        clean = clean.replace("_", "") // underscores
        clean = clean.replace("~", "") // tildes de formato
        
        // Remover backticks
        clean = clean.replace("`", "")
        
        // Espacios múltiples a uno solo
        clean = clean.replace(Regex("\\s+"), " ")
        
        return clean.trim()
    }
    
    private fun getPredefinedResponse(message: String): String {
        return when {
            message.contains("hola") || message.contains("buenos") || message.contains("hi") -> {
                lastTopic = ""
                """¡Hola! 👋 Soy el asistente de NGD Tech Solutions.
Somos especialistas en desarrollo de apps móviles con IA y soporte técnico.
¿En qué puedo ayudarte hoy?"""
            }

            message.contains("estado") || message.contains("progreso") || message.contains("versión") || message.contains("consultar el estado") -> {
                lastTopic = "estado"
                """📊 Para consultar el estado de tu proyecto:
- ¿Qué aplicación te gustaría consultar?
- O escribe el nombre de tu proyecto

Mientras tanto, ¿hay algo más en lo que pueda ayudarte?"""
            }

            message.contains("error") || message.contains("problema") || message.contains("bug") || message.contains("no funciona") || message.contains("tengo un problema") || message.contains("falla") || message.contains("crash") || message.contains("cierra") -> {
                lastTopic = "error"
                """😟 Lamento que tengas un problema. Para ayudarte de la mejor manera:

1. ¿Qué app o funcionalidad falla? (ej: chat, login, cámara)
2. ¿Qué mensaje de error ves? (copia el texto exacto)
3. ¿Cuándo empezó? (después de una actualización, siempre, etc.)

También puedes adjuntar una captura de pantalla si es posible."""
            }

            message.contains("presupuesto") || message.contains("presupuestos") || message.contains("precio") || message.contains("precios") || message.contains("coste") || message.contains("costo") || message.contains("cuanto") || message.contains("cuánto") || message.contains("solicitar") || message.contains("solicitud") || message.contains("plazo") || message.contains("fecha") || message.contains("cuando") || message.contains("cuándo") || message.contains("tiempo") || message.contains("deadline") || message.contains("vencimiento") || message.contains("entrega") || message.contains("cotización") || message.contains("cotizar") -> {
                lastTopic = "presupuesto"
                if (message.contains("plazo") || message.contains("fecha") || message.contains("cuando") || message.contains("tiempo") || message.contains("deadline") || message.contains("vencimiento") || message.contains("entrega")) {
                    """📅 Perfecto, anoto el plazo. 
¿Tienes alguna fecha específica en mente? Por ejemplo: "lo necesito para junio" o "en 3 meses".

También necesito saber:
• Tipo de proyecto (app móvil, web, ambos)
• Funcionalidades principales

Con eso te puedo dar un presupuesto más exacto."""
                } else {
                    """💰 Genial, para darte un presupuesto necesito que me cuentes:
1. Tipo de proyecto: ¿App móvil, web o ambos?
2. Funcionalidades: ¿Qué debe hacer la app?
3. Plazo: ¿Cuándo lo necesitas?
4. Diseño: ¿Ya lo tienes o necesitas que te ayudemos?

Cuéntame sobre tu idea y te prepararé una cotización personalizada."""
                }
            }

            message.contains("servicio") || message.contains("qué haces") || message.contains("qué ofrecen") -> {
                lastTopic = "servicios"
                """📱 NGD Tech Solutions ofrece:

• Desarrollo de Apps (Android iOS)
• Integración de Inteligencia Artificial
• Soporte técnico y mantenimiento
• Consultorías técnicas
• Publicación en stores

¿Sobre qué servicio necesitas más información?"""
            }

            message.contains("quién") || message.contains("empresa") || message.contains("sobre ti") -> {
                lastTopic = "empresa"
                """🏢 NGD Tech Solutions

Somos una empresa especializada en desarrollo de apps móviles con IA.
Atendemos a:
• Empresas de comercio
• Hostelería
• Informática
• Logística
• Freelancers y autónomos

Nuestro objetivo es hacer tecnología accesible para todos."""
            }

            message.contains("hablar con agente") || message.contains("persona") || message.contains("humano") || message.contains("agente humano") -> {
                lastTopic = ""
                """👤 Perfecto, te conecto con un agente humano ahora mismo.

Mientras tanto, ¿podrías contarme brevemente sobre tu proyecto o problema? Así el agente tendrá contexto y podrá ayudarte más rápido.

*Un representante se comunicará contigo en breve.*"""
            }

            message.contains("urgente") || message.contains("ahora") || message.contains("rápido") -> {
                lastTopic = "urgente"
                """⚠️ Entiendo que es urgente.

He marcado tu caso como prioritario.
Un agente humano te contactará lo antes posible.
¿Hay algún detalle adicional que debamos saber?"""
            }

            message.contains("gracias") || message.contains("adiós") || message.contains("bye") -> {
                lastTopic = ""
                """¡De nada! 😊

Si tienes más dudas, no dudes en escribir.
¡Un saludo de parte de NGD Tech Solutions!
🤖"""
            }

            else -> {
                when (lastTopic) {
                    "presupuesto" -> {
                        """📊 Perfecto, sigamos con tu presupuesto.

¿Podrías darme más detalles sobre:
• Funcionalidades principales que necesitas
• Plazo deseado para el proyecto

Con eso te prepararé una cotización personalizada."""
                    }
                    "error" -> {
                        """🔍 Para solucionar tu problema, necesito saber:

• ¿Qué exactamente falla? (ej: "no carga el login")
• ¿Cuándo empezó? (después de actualizar, siempre, etc.)

Cuantos más detalles, más rápido puedo ayudarte."""
                    }
                    else -> {
                        """¿En qué puedo ayudarte?

• Presupuesto - "quiero un presupuesto para una app"
• Error - "mi app no funciona"
• Estado - "consultar estado de mi proyecto"
• Agente - "hablar con agente"

Sé específico y te ayudaré mejor."""
                    }
                }
            }
        }
    }
}
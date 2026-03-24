package com.example.ngdtechsupport.ai

object AiResponseHelper {

    const val USE_OPENAI = false

    private var lastTopic: String = ""

    fun getResponse(userMessage: String): String {
        val message = userMessage.lowercase().trim()
        if (USE_OPENAI) {
            return cleanResponse("Integrando con OpenAI.")
        }
        return cleanResponse(getPredefinedResponse(message))
    }

    private fun cleanResponse(response: String): String {
        var clean = response
            .replace(Regex("[*_`~]+"), "")
            .replace(Regex("#{1,6}\\s*"), "")
            .replace(Regex("/{2,}"), " ")
            .replace("•", "-")

        clean = clean.replace(Regex("[^\\p{L}\\p{N}\\s\\.,;:!¿?()\"'\\-\\n]"), "")
        clean = clean.replace(Regex("[!?]{2,}")) { it.value.take(1) }
        clean = clean.replace(Regex("([.,;:]){2,}")) { it.value.take(1) }
        clean = clean.replace(Regex("\\s+([.,;:!?])"), "$1")
        clean = clean.replace(Regex("([¿¡])\\s+"), "$1")
        clean = clean.replace(Regex("[ \\t]{2,}"), " ")
        clean = clean.replace(Regex("\\n{3,}"), "\n\n")
        clean = clean.trim()

        if (clean.isNotEmpty() && clean.last() !in listOf('.', '!', '?')) {
            clean += "."
        }

        return clean
    }

    private fun getPredefinedResponse(message: String): String {
        return when {
            message.contains("hola") || message.contains("buenos") || message.contains("hi") -> {
                lastTopic = ""
                "Hola. Soy el asistente de NGD Tech Solutions. En que puedo ayudarte hoy?"
            }

            message.contains("estado") || message.contains("progreso") || message.contains("version") || message.contains("versión") -> {
                lastTopic = "estado"
                "Para revisar el estado de tu proyecto, dime el nombre de la app o del negocio."
            }

            message.contains("error") || message.contains("problema") || message.contains("bug") || message.contains("no funciona") || message.contains("falla") -> {
                lastTopic = "error"
                "Entendido. Para ayudarte mejor necesito: 1) qué función falla, 2) mensaje de error exacto, 3) cuándo empezó."
            }

            message.contains("presupuesto") || message.contains("precio") || message.contains("coste") || message.contains("costo") || message.contains("cotizacion") || message.contains("cotización") || message.contains("solicitar") -> {
                lastTopic = "presupuesto"
                "Perfecto. Para prepararte un presupuesto necesito tipo de proyecto, funcionalidades principales, plazo y si ya tienes diseño."
            }

            message.contains("plazo") || message.contains("fecha") || message.contains("cuando") || message.contains("cuándo") || message.contains("tiempo") || message.contains("entrega") || message.contains("deadline") -> {
                if (lastTopic == "presupuesto") {
                    "Perfecto, anoto el plazo. También dime funcionalidades principales y tipo de proyecto para cerrarte la estimación."
                } else {
                    "Dime para que proyecto necesitas ese plazo y te ayudo a planificarlo."
                }
            }

            message.contains("agente") || message.contains("humano") || message.contains("persona") -> {
                lastTopic = ""
                "De acuerdo, te paso con un agente humano. Describe brevemente tu caso y te atenderemos enseguida."
            }

            message.contains("gracias") || message.contains("adios") || message.contains("adiós") -> {
                lastTopic = ""
                "Gracias a ti. Si necesitas algo mas, escribeme cuando quieras."
            }

            else -> {
                when (lastTopic) {
                    "presupuesto" -> "Seguimos con el presupuesto. Dime funcionalidades, plazo y tipo de proyecto."
                    "error" -> "Seguimos con el problema. Dime el error exacto y en que pantalla ocurre."
                    "estado" -> "Dime el nombre del proyecto para revisar su estado actual."
                    else -> "Puedo ayudarte con estado de proyecto, reportar errores, presupuesto o hablar con agente."
                }
            }
        }
    }
}

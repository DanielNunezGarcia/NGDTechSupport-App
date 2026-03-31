# Guía de Pruebas - Funcionalidad IA

## Estado Actual

La IA funciona con respuestas Mock (simuladas) ya que Cloud Functions requiere plan Blaze. 
El `AiResponseHelper` tiene `USE_OPENAI = false` y usa respuestas predefinidas.

## Componentes Principales

1. **AiResponseHelper.kt**: Genera respuestas basadas en palabras clave en el mensaje del usuario.
2. **ChatViewModel.sendWelcomeMessageOnOpen()**: Envía un mensaje de bienvenida aleatorio al abrir el chat.
3. **ChatViewModel.processMessageWithAi()**: Genera respuesta de IA cuando el usuario envía un mensaje.

## Cómo Probar la IA

### Prueba 1: Mensaje de Bienvenida Aleatorio

1. Abre la app y navega a un chat.
2. Observa que al entrar, aparece un mensaje de bienvenida de la IA.
3. Cierra el chat y vuelve a abrirlo.
4. Verifica que el mensaje de bienvenida puede ser diferente (aleatorio de una lista).
5. Si no hay mensajes configurados en Firestore, se usa el mensaje por defecto.

### Prueba 2: Respuestas de IA a Mensajes

1. En el chat, envía un mensaje como "Hola".
2. Verifica que la IA responde con "Hola. Soy el asistente de NGD Tech Solutions. En que puedo ayudarte hoy?".
3. Envía "tengo un error".
4. Verifica que la IA responde pidiendo detalles del error.
5. Envía "presupuesto".
6. Verifica que la IA pide detalles del proyecto.
7. Envía "agente humano".
8. Verifica que la IA ofrece transferir a un agente.

### Prueba 3: Configuración de IA (Admin)

1. Como administrador, ve a Configuración > Configuración IA.
2. Verifica que la sección "Mensajes de Bienvenida" está ocultada (no visible).
3. Configura otros campos como "Mensaje de Fallback", "Quick Replies", etc.
4. Guarda la configuración.
5. Verifica que la configuración se guarda correctamente en Firestore.

### Prueba 4: Validación de Respuestas Legibles

1. Envía varios mensajes de prueba.
2. Verifica que las respuestas de IA no contienen caracteres extraños, marcado de markdown, etc.
3. Confirma que las respuestas son oraciones completas y legibles.

## Notas Técnicas

- Las respuestas Mock están definidas en `AiResponseHelper.getPredefinedResponse()`.
- Los mensajes de bienvenida se almacenan en Firestore en `companies/{companyId}/ai_config/settings`.
- Si no hay mensajes de bienvenida configurados, se usa un mensaje por defecto.
- La IA responde inmediatamente después de que el usuario envía un mensaje (simulado).

## Solución de Problemas

- **No aparece mensaje de bienvenida**: Verificar que la IA está habilitada en la configuración.
- **No hay respuesta de IA**: Verificar que `AiResponseHelper.USE_OPENAI` es `false` y que el mensaje del usuario coincide con alguna palabra clave.
- **Respuestas con caracteres raros**: Verificar el método `cleanResponse()` en `AiResponseHelper`.

## Para Desarrolladores

Para cambiar las respuestas Mock, modificar `AiResponseHelper.getPredefinedResponse()`.
Para cambiar el mensaje de bienvenida por defecto, modificar la lista en `ChatViewModel.sendWelcomeMessageOnOpen()`.
# NGD TechSupport App

Aplicación Android de soporte técnico para NGD Tech Solutions con chat híbrido (IA + humano), gestión de canales, updates por negocio y panel admin web.

## Estado del Proyecto

- FASE 1-6: Chat humano base
- FASE 7: IA integrada (respuesta asistida)
- FASE 8: Seguridad y reglas
- FASE 9: Panel web/admin
- FASE 10: Pulido final (estabilidad, UX, rendimiento básico)

## Tecnologías

- Android (Kotlin)
- Firebase Auth + Firestore + Messaging
- ViewModel + LiveData + Coroutines
- Netlify (panel web)

## Estructura Relevante

- `app/` Android app principal
- `web/admin/` panel web de administración
- `docs/` documentación por fases
- `agents/` definiciones de agentes
- `mcp/` configuración MCP

## Configuración Local

1. Instala JDK 21
2. Configura Firebase (`app/google-services.json`)
3. Variables web en `.env`
4. Compila:

```bash
./gradlew.bat assembleDebug
```

APK debug generada en:

- `app/build/outputs/apk/debug/app-debug.apk`

## Flujos Principales

- Admin: Dashboard -> Negocio -> Chat/Updates/Canales -> Config IA
- Cliente: Dashboard -> Su negocio -> Chat/Updates

## Documentación

- Fase 10: `docs/FASE10_PULIDO_FINAL.md`
- Sistema de agentes: `AGENTS.md`

## Notas

- El repositorio usa flujo por agentes (supervisor + especializados) vía MCP.
- Si no ves cambios en local, actualiza rama con `git pull` y valida rama actual con `git status`.

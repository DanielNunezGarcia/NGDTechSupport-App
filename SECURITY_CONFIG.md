# Configuración de Seguridad - NGD Tech Support

## Variables de Entorno (Firebase Secrets)

```bash
# Secrets para Firebase Functions
firebase functions:secrets:set OPENAI_API_KEY
```

## Configuración en firebase.json

```json
{
  "functions": {
    "runtime": "nodejs24",
    "source": "functions",
    "codebase": "default",
    "ignore": [
      "node_modules",
      ".git",
      "firebase-debug.log",
      "firebase-debug.*.log"
    ],
    "predeploy": [
      "npm --prefix functions run lint"
    ],
    "securityRules": {
      "source": "firestore.rules"
    }
  },
  "firestore": {
    "rules": "firestore.rules",
    "indexes": "firestore.indexes.json"
  }
}
```

## Dependencias requeridas (package.json)

```json
{
  "dependencies": {
    "firebase-admin": "^13.6.0",
    "firebase-functions": "^7.0.0",
    "openai": "^4.77.0"
  }
}
```

## Recomendaciones de Seguridad Adicionales

### 1. Almacenamiento de API Key

- **USAR Firebase Secrets**: Nunca almacenar la API Key en código
- **Rotación periódica**: Cambiar keys cada 90 días
- **Restringir en Dashboard OpenAI**: Limitar IP y dominios permitidos
- **Monitoreo de uso**: Revisar consumo en panel de OpenAI

### 2. Rate Limiting

- **Límite por usuario**: 20 requests/minuto, 100k tokens/día
- **Límite global**: Configurar en Firebase si es necesario
- **Logging**: Registrar intentos fallidos para análisis

### 3. Validación de Entrada

- **Sanitización**: Eliminar caracteres especiales peligrosos
- **Longitud máxima**: 10,000 caracteres por mensaje
- **Patrones bloqueados**: XSS, injection, scripts

### 4. Protección de Datos

- **Cifrado en tránsito**: HTTPS obligatorio
- **No guardar prompts sensibles**: Configuración de retención
- **Anonimización**: Eliminar PII de logs

### 5. Cloud Function Security

```javascript
// Configuración recomendada
exports.chatWithAI = functions
  .runWith({
    secrets: ["OPENAI_API_KEY"],
    memory: "512MB",
    timeoutSeconds: 60,
    ingressSettings: "ALLOW_INTERNAL_AND_AUTHENTICATED",
  })
  .https.onCall(async (data, context) => {
    // Tu código aquí
  });
```

### 6. Configuración de Ingress

En firebase.json:
```json
{
  "functions": {
    "ingressSettings": {
      "source": "Authenticated"
    }
  }
}
```

### 7. Monitoreo y Alertas

- **Cloud Logging**: Registrar todas las llamadas a IA
- **Alertas de presupuesto**: Configurar en Firebase
- **Detección de anomalías**: Monitorear patrones inusuales

### 8. Políticas de CORS

```javascript
// En index.js
exports.chatWithAI = functions
  .runWith({ secrets: ["OPENAI_API_KEY"] })
  .https.onCall((data, context) => {
    // Solo permitir requests autenticados
    // CORS manejado automáticamente por Firebase
  });
```

### 9. Limpieza de Memoria

- Implementar limpieza de Rate Limit store periódicamente
- No almacenar historial de conversaciones en memoria

### 10. Documentación

- Mantener registro de cambios en políticas
- Entrenar usuarios en uso responsable
- Definir casos de uso aceptados

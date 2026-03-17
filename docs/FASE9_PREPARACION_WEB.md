# Documento de Diseño - Fase 9: Preparación Web

## 1. Estructura de Datos Web-Compatible

### 1.1 Estructura Actual de Firestore (Reutilizable)

La estructura actual ya es web-compatible:

```
companies/{companyId}/
├── businesses/          → Empresas del tenant
├── channels/           → Canales de chat
├── messages/           → Mensajes del chat
├── chat/               → Estado del chat
├── chatStatus/         → Estado activo del chat
├── ai_config/          → Configuración de IA
│   └── settings/       → settings (documento único)
└── updates/            → Actualizaciones del sistema
users/{userId}          → Usuarios (colección raíz)
```

### 1.2 Recomendaciones de Optimización

**Campos de compatibilidad web:**
- Añadir campo `createdAt` y `updatedAt` (timestamps) a todas las colecciones
- Estandarizar nombres de campos (camelCase)
- Evitar documentos anidados muy profundos (máx 2 niveles)

**Índices recomendados:**
- `companies` → índice en `createdAt` para ordenación
- `channels` → índice compuesto `[companyId, createdAt]`
- `messages` → índice compuesto `[channelId, timestamp]`

---

## 2. API REST Opcional

### 2.1 Cuándo Usar API REST vs SDK Firebase

| Escenario | Recomendación |
|----------|----------------|
| Panel admin web simple | SDK Firebase (Admin SDK) |
| Web pública/sin backend propio | SDK Firebase Client |
| Necesidad de transformed data | API REST con Cloud Functions |
| Integración con sistemas externos | API REST |

### 2.2 Arquitectura Propuesta (Sin Blaze)

**Opción A: Cloud Functions (Blaze requerido)**
```
Web → Firebase Admin SDK → Firestore
         ↑ requiere Blaze
```

**Opción B: Firebase REST API (Spark)**
```
Web → Firebase REST API → Firestore
         (sin Blaze, pero limitaciones)
```

**Opción C: Firebase Admin SDK en servicio externo**
```
Web → Backend propio → Firebase Admin SDK → Firestore
         (puede ser serverless: Cloud Run, etc.)
```

### 2.3 Endpoints Sugeridos (Si se necesita REST)

```
GET    /api/v1/companies
GET    /api/v1/companies/{id}
GET    /api/v1/companies/{id}/channels
GET    /api/v1/channels/{id}/messages
POST   /api/v1/channels/{id}/messages
PATCH  /api/v1/companies/{id}/ai_config
GET    /api/v1/users
```

---

## 3. Componentes del Panel Admin Web

### 3.1 Módulos Recomendados

| Módulo | Descripción | Prioridad |
|--------|-------------|-----------|
| **Dashboard** | Métricas, tickets activos, usuarios | Alta |
| **Gestión de Canales** | Ver/editar canales de chat | Alta |
| **Chat en Vivo** | Responder chats como agente | Alta |
| **Configuración IA** | Ajustar prompts, comportamientos | Alta |
| **Gestión de Empresas** | CRUD de empresas/tenants | Media |
| **Historial de Chats** | Búsqueda y filtrado de conversaciones | Media |
| **Estadísticas** | Charts, reportes, analytics | Baja |
| **Usuarios Admin** | Gestión de usuarios del panel | Media |

### 3.2 Autenticación

**Opciones (sin Blaze):**
1. **Firebase Auth** (Client SDK) → Funciona con Spark
2. **Custom JWT** → Con tu propio backend
3. **Email/Password en Firestore** → No recomendado

**Recomendación:** Usar Firebase Auth con email/password o Google Sign-In.

### 3.3 Estructura de Datos para Panel

**Colección adicional recomendada:**
```
admins/{adminId}        → Usuarios del panel admin
├── email: string
├── role: string        → "super_admin" | "agent" | "viewer"
├── companyId: string   → Company asociado
└── createdAt: timestamp
```

---

## 4. Opción Sin Blaze - Hosting y Backend

### 4.1 Opciones de Hosting Gratuito

| Opción | Costo | Limitaciones |
|--------|-------|--------------|
| **Firebase Hosting (Spark)** | Gratis | Solo hosting estático |
| **Vercel** | Gratis | Solo frontend estático |
| **Netlify** | Gratis | Solo frontend estático |
| **GitHub Pages** | Gratis | Solo frontend estático |

### 4.2 Solución Sin Blaze

**Arquitectura propuesta:**
```
[Panel Web Estático] → [Firebase Client SDK] → [Firestore]
        ↓
   (solo lectura de datos públicos)
   
[Panel Web Estático] → [Cloudflare Workers] → [Firestore REST API]
        ↓
   (backend serverless sin Firebase)
```

**Opción recomendada:**
1. **Frontend estático** en Vercel/Netlify (gratis)
2. **Firebase Client SDK** para acceso a Firestore (Spark)
3. **Reglas de Firestore** para seguridad

### 4.3 Reglas de Firestore para Panel Admin

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Panel admin: solo lectura/escritura con autenticación
    match /companies/{companyId}/{document=**} {
      allow read, write: if request.auth != null 
        && request.auth.token.role in ['super_admin', 'agent']
        && request.auth.token.companyId == companyId;
    }
    
    // Chat: agentes pueden leer/escribir
    match /companies/{companyId}/channels/{channelId}/messages/{msgId} {
      allow read, write: if request.auth != null 
        && request.auth.token.companyId == companyId;
    }
    
    // Solo super_admin puede gestionar admins
    match /admins/{adminId} {
      allow read, write: if request.auth != null 
        && request.auth.token.role == 'super_admin';
    }
  }
}
```

---

## 5. Roadmap de Implementación

### Fase 9.1: Preparación Datos (Inmediata)
- [ ] Añadir campos `createdAt`/`updatedAt` a colecciones
- [ ] Crear colección `admins`
- [ ] Crear índices necesarios

### Fase 9.2: Panel Mínimo Viable (Post-Blaze)
- [ ] Desplegar Cloud Functions (requiere Blaze)
- [ ] Implementar endpoint REST básico
- [ ] Frontend React/Vue en Firebase Hosting

### Fase 9.3: Expansión (Opcional)
- [ ] Dashboard con analytics
- [ ] Chat en vivo desde web
- [ ] Configuración de IA desde panel

---

## 6. Consideraciones de Seguridad

1. **Nunca expongas Firebase API key** en frontend de producción
2. **Usa reglas de Firestore** restrictivas
3. **Implementa rate limiting** si usas Cloud Functions
4. **Valida todos los datos** en backend (no confíes en cliente)
5. **Usa HTTPS** siempre (obligatorio en Firebase)

---

## 7. Tecnologías Recomendadas para Web

| Capa | Tecnología | Notas |
|------|------------|-------|
| Framework | React o Vue 3 | Más popular, buen ecosistema |
| UI Library | Tailwind CSS + shadcn/ui | Flexible, rápido |
| State | Zustand o TanStack Query | Para gestión de estado |
| Firebase | Firebase JS SDK v9+ | Modular, tree-shaking |
| Charts | Recharts o Chart.js | Para analytics |
| Forms | React Hook Form | Validación simple |

---

*Documento generado para Fase 9 - Preparación Web*
*Compatible con Firebase Spark Plan (sin Blaze)*
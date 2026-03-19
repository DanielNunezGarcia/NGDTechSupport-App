# NGD TechSupport

## Descripción del Proyecto

NGD TechSupport es una aplicación móvil de soporte técnico con asistencia inteligente basada en Inteligencia Artificial. El proyecto combina un sistema de chat híbrido que permite a los clientes interactuar tanto con un agente de IA como con soporte humano, ofreciendo una experiencia de atención al cliente moderna y eficiente.

La aplicación está diseñada para que empresas de cualquier sector puedan gestionar sus tickets de soporte técnico, proporcionando un panel de administración web para supervisar conversaciones, configurar el comportamiento del agente IA, y gestionar actualizaciones de aplicaciones.

## Arquitectura

### Stack Tecnológico

El proyecto utiliza las siguientes tecnologías principales:

- **Plataforma Móvil**: Android (Kotlin)
- **Backend**: Firebase (Firestore, Authentication, Cloud Messaging)
- **Base de Datos Local**: SQLite (Room Database)
- **Panel Web Admin**: Vanilla JavaScript + Firebase SDK
- **Funciones Serverless**: Firebase Cloud Functions
- **Despliegue Web**: Netlify

### Diagrama de Arquitectura

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENTE MÓVIL                            │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐ │
│  │   Android   │  │   Firebase  │  │     SQLite Local        │ │
│  │   (Kotlin)  │──│  (Firestore)│  │   (Room Database)       │ │
│  └─────────────┘  └──────┬──────┘  └─────────────────────────┘ │
│                          │                                      │
│                   ┌──────▼──────┐                               │
│                   │ Auth        │                               │
│                   │ Firebase    │                               │
│                   └─────────────┘                               │
└─────────────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                        SERVIDOR / NUBE                           │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐ │
│  │  Firestore  │  │ Cloud       │  │    Firebase            │ │
│  │  Database   │  │ Functions   │  │    Authentication      │ │
│  └─────────────┘  └─────────────┘  └─────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                        PANEL WEB ADMIN                          │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │           https://ngd-techsupport-admin.netlify.app        ││
│  └─────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────┘
```

## Estructura del Proyecto

El proyecto se organiza en las siguientes carpetas principales:

```
NGD_TechSupport/
├── app/                          # Aplicación Android
│   └── src/main/
│       ├── java/com/example/ngdtechsupport/
│       │   ├── ai/               # Módulo de Inteligencia Artificial
│       │   │   ├── AiChatActivity.kt
│       │   │   ├── AiChatViewModel.kt
│       │   │   ├── AiChatAdapter.kt
│       │   │   ├── AiResponseHelper.kt
│       │   │   ├── AiRepository.kt
│       │   │   ├── AiConversation.kt
│       │   │   └── SendMessageToAiUseCase.kt
│       │   ├── data/             # Capa de datos
│       │   │   ├── local/        # Base de datos SQLite
│       │   │   ├── ChatRepository.kt
│       │   │   ├── UserRepository.kt
│       │   │   ├── AppRepository.kt
│       │   │   └── ChannelRepository.kt
│       │   ├── model/            # Modelos de datos
│       │   ├── ui/               # Interfaz de usuario
│       │   │   ├── auth/         # Autenticación
│       │   │   ├── chat/         # Chat con agentes
│       │   │   ├── channel/      # Canales de comunicación
│       │   │   ├── dashboard/    # Pantalla principal
│       │   │   ├── admin/        # Configuración admin
│       │   │   └── updates/      # Gestión de actualizaciones
│       │   ├── utils/            # Utilidades
│       │   └── domain/           # Casos de uso
│       └── res/                  # Recursos (layouts, drawables, etc.)
├── web/
│   └── admin/                    # Panel de administración web
│       ├── index.html
│       ├── app.js
│       ├── firebase-config.js
│       └── style.css
├── functions/                    # Firebase Cloud Functions
│   ├── index.js
│   ├── security/
│   └── package.json
├── docs/                         # Documentación
├── gradle/                       # Configuración de build
└── Documentation/                # Documentos del proyecto
```

### Módulos Principales

1. **Módulo AI**: Gestiona las interacciones con el agente de Inteligencia Artificial, incluyendo respuestas automáticas y configuración del modelo.

2. **Módulo de Datos**: Maneja la comunicación con Firebase Firestore y la base de datos local SQLite, implementando el patrón Repository.

3. **Módulo UI**: Contains todas las actividades, adaptadores y viewmodels de la aplicación Android.

4. **Módulo Web Admin**: Panel web para administrators con funcionalidades de gestión de conversaciones y configuración.

5. **Módulo Functions**: Funciones serverless de Firebase para lógica de negocio adicional.

## Características Implementadas

### Chat Híbrido (IA + Humano)

El sistema de chat híbrido permite a los usuarios elegir entre:

- **Chat con IA**: Un agente inteligente capaz de responder preguntas frecuentes, guiar al usuario a través de problemas técnicos comunes, y escalar a un agente humano cuando sea necesario.

- **Chat con Agente Humano**: Conversación directa con el equipo de soporte técnico de la empresa.

- **Canales Privados**: Posibilidad de crear canales de comunicación privados entre clientes y agentes específicos.

### Panel Web Admin

El panel de administración web proporciona las siguientes funcionalidades:

- Visualización de todas las conversaciones en tiempo real
- Gestión de conversaciones y mensajes
- Configuración del agente IA
- Panel de control con estadísticas
- Interfaz de chat para responder a clientes

### Base de Datos Local (SQLite)

La aplicación utiliza SQLite mediante Room Database para:

- Almacenamiento offline de conversaciones
- Cacheo de datos para mejor rendimiento
- Sincronización con Firestore cuando hay conexión

### Sistema de Roles

El sistema de autenticación y autorización incluye:

- **Rol Administrador**: Acceso completo al panel web y configuración de la app
- **Rol Cliente**: Acceso a la aplicación móvil para crear tickets y chatear

## Usuarios y Roles

### Cuentas de Prueba

| Email | Rol | Descripción |
|-------|-----|-------------|
| admin@test.com | Admin | Cuenta de administrador de pruebas |
| admin@ngd.com | Admin | Cuenta de administrador principal |
| cliente@restaurante.com | Cliente | Cuenta de cliente de ejemplo |

### Permisos por Rol

**Administrador (Admin)**:
- Acceso completo al panel web admin
- Gestión de todas las conversaciones
- Configuración del agente IA
- Publicación de actualizaciones de apps
- Ver estadísticas y dashboard

**Cliente**:
- Iniciar conversaciones de soporte
- Chatear con IA o agente humano
- Recibir notificaciones de actualizaciones
- Ver estado de sus tickets

## Cómo Ejecutar

### Aplicación Android

1. **Requisitos Previos**:
   - Android Studio (versión 2024.1 o superior)
   - JDK 17
   - Android SDK 34

2. **Pasos para ejecutar**:
   - Abrir el proyecto en Android Studio
   - Seleccionar "Open Existing Project" y navegar a la carpeta `NGD_TechSupport`
   - Esperar a que Gradle sincronice las dependencias
   - Conectar un dispositivo Android o crear un emulador
   - Ejecutar con el botón "Run" o presionar Shift + F10

3. **Configuración de Firebase**:
   - Crear un proyecto en Firebase Console
   - Descargar el archivo `google-services.json`
   - Colocarlo en la carpeta `app/`
   - Habilitar Authentication, Firestore y Cloud Messaging

### Panel Web Admin

1. **Despliegue en Netlify**:
   - Acceder a https://app.netlify.com
   - Conectar el repositorio o subir la carpeta `web/admin`
   - Netlify detectará automáticamente la configuración

2. **Acceso**:
   - URL: https://ngd-techsupport-admin.netlify.app
   - Iniciar sesión con credenciales de admin

3. **Configuración**:
   - Actualizar `firebase-config.js` con las credenciales del proyecto Firebase
   - Configurar las reglas de Firestore para permitir lectura/escritura

### Firebase Cloud Functions

1. **Instalar Firebase CLI**:
   ```bash
   npm install -g firebase-tools
   ```

2. **Desplegar funciones**:
   ```bash
   cd functions
   firebase deploy --only functions
   ```

## Contribución y Desarrollo

### Estructura de Branches

- `main`: Rama principal de producción
- `develop`: Rama de desarrollo
- `feature/*`: Ramas para nuevas funcionalidades
- `fix/*`: Ramas para correcciones de bugs

### Commits

Seguimos el conventional commits:
- `feat:` Nuevas funcionalidades
- `fix:` Correcciones de bugs
- `docs:` Documentación
- `refactor:` Refactorización de código

## Fases del Proyecto

| Fase | Descripción | Estado |
|------|-------------|--------|
| FASE 1-6 | Chat humano funcionando | ✅ Completada |
| FASE 7 | Chat con IA (Mock responses) | ✅ Completada |
| FASE 8 | Seguridad (Firestore rules) | ✅ Completada |
| FASE 9 | Preparación Web (panel admin) | ✅ Completada |
| FASE 10 | Pulido Final (optimización) | 📋 En progreso |

---

## Licencia

Este proyecto es propiedad de NGD Studios y está destinado exclusivamente para fines de demostración y desarrollo.

---

**NGD Studios - Tech Solutions**

*Desarrollado con ❤️ por el equipo de NGD Studios*

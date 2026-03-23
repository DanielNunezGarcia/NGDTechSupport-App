# Sistema de Agentes - NGD TechSupport

## Overview

Este proyecto utiliza un sistema de agentes para divided the workload. El supervisor asigna tareas a agentes especializados.

## Agentes Disponibles

| Agente | Rol | Especialidad |
|--------|-----|--------------|
| **supervisor** | Coordinator | Asigna tareas y revisa resultados |
| **android_dev_01** | Android Senior | Kotlin/Java, funcionalidades |
| **android_dev_02** | Android Jetpack | Kotlin, UI/Jetpack |
| **backend** | Backend | Firebase, APIs |
| **ui** | UI/UX | Diseño, layouts |
| **qa** | QA Lead | Testing strategy |
| **qa_tester** | QA Tester | Pruebas funcionales |
| **support** | Support | Incidencias en producción |
| **security** | Security | Seguridad, permisos |
| **performance** | Performance | Optimización |
| **devops** | DevOps | Build, deploy |
| **documentation** | Docs | Documentación |
| **architect** | Architect | Arquitectura |

## Cómo Usar los Agentes

### Paso 1: El Supervisor analiza la tarea

El supervisor (tú) divide la tarea en subtareas y determina qué agente la debe realizar.

### Paso 2: Asignar tarea al agente

Usa la herramienta `task` con el tipo `general` para ejecutar tareas específicas:

```
task(description="[descripción]", prompt="[tarea]", subagent_type="general")
```

### Paso 3: El agente ejecuta y reporta

El agente reporta qué hizo y si tuvo éxito.

### Paso 4: Supervisor verifica y continúa

## Workflows

### Nueva Funcionalidad
```
supervisor → architect → android_dev_01 + android_dev_02 → backend → qa → devops
```

### Bug en Producción
```
supervisor → support → qa_tester → android_dev_01 → backend → qa → devops
```

### Soporte Cliente
```
supervisor → support → qa_tester → android_dev_01
```

### Optimización
```
supervisor → performance → android_dev_02 → backend → qa
```

## Ejemplos de Uso

### Asignar bug al agente de Android
```python
task(
    description="Fix chat scroll bug",
    prompt="Arreglar el bug de scroll en el chat de Android. El proyecto está en D:\\Proyectos\\APP\\NGDStudios\\NGD_TechSupport. Cuando termines, verifica que compile con ./gradlew.bat assembleDebug",
    subagent_type="general"
)
```

### Asignar tarea de UI
```python
task(
    description="Improve chat colors",
    prompt="Mejorar los colores del chat en la app Android. Cambiar los colores de las burbujas: usuario=azul, IA=naranja, agente=cyan. El proyecto está en D:\\Proyectos\\APP\\NGDStudios\\NGD_TechSupport",
    subagent_type="general"
)
```

## Reglas de Ejecución

1. **Siempre verificar build** después de cambios
2. **Hacer commit** después de cada fix exitoso
3. **Reportar al usuario** qué se hizo
4. **Si falla,报告ar el error**

## MCP Configuration

El MCP está configurado para workflows pero los agentes se ejecutan via la herramienta `task` con `subagent_type="general"`.

Para ejecutar múltiples agentes en paralelo, iniciar múltiples tareas.

---

*Última actualización: 2026-03-20*

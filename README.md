# eScrims — Plataforma de Matchmaking para Scrims de eSports

Sistema integral de gestión, emparejamiento (matchmaking) y ciclo de vida de partidas competitivas (*scrims*) para eSports (Valorant, League of Legends, CS2), diseñado bajo principios de **Análisis y Diseño Orientado a Objetos (ADOO)**, **Clean Architecture**, **SOLID/GRASP** y **Patrones de Diseño GoF**.

---

## 🏛️ Arquitectura y Estructura de Capas

El proyecto sigue una separación estricta de capas desacopladas conforme a la **Clean Architecture** (las dependencias apuntan exclusivamente hacia adentro):

```
com.escrims/
├── EscrimsApplication.java                # Punto de entrada Spring Boot
├── controller/                           # Capa Web / API REST
│   ├── dto/                              # Records con Bean Validation (JSR-380)
│   └── exception/                        # GlobalExceptionHandler (RFC 7807)
├── domain/                               # Núcleo de Dominio Puro (POJOs, sin deps externas)
│   ├── builder/                          # «Builder» ScrimBuilder (RN-01 a RN-08)
│   ├── command/                          # «Command» ScrimCommand, Invoker y comandos reversibles (RN-12)
│   ├── entities/                         # Entidades, Value Objects y Enums del modelo de dominio
│   ├── moderation/                       # «Chain of Responsibility» Handlers de moderación y sanciones
│   ├── observer/                         # «Observer» DomainEventBus, Subscriber y eventos de dominio
│   ├── repository/                       # Interfaces de repositorios del dominio (DIP)
│   ├── state/                            # «State» ScrimContext y los 6 estados del ciclo de vida
│   ├── strategy/                         # «Strategy» Algoritmos de Matchmaking (MMR, Latencia, Historial)
│   └── validation/                       # «Template Method» Validadores de configuración por juego
├── service/                              # Capa de Servicios de Aplicación
│   ├── ScrimService.java                 # Fachada coordinadora de Scrims
│   ├── MatchmakingService.java           # Contexto del patrón Strategy
│   └── moderation/                       # Subpaquete de servicios de moderación
│       └── ModerationService.java        # Orquestador del pipeline de moderación
└── infrastructure/                       # Capa de Infraestructura y Adaptadores
    ├── config/                           # Configuración de beans de Spring (AppBeansConfig, SchedulingConfig)
    ├── notification/                     # «Abstract Factory» + «Adapter» y Suscriptores de notificación
    ├── persistence/                      # Persistencia relacional Spring Data JPA + H2
    │   ├── entity/                       # Entidades JPA (*JpaEntity)
    │   └── repository/                   # Repositorios JPA Spring Data (*JpaRepository)
    └── scheduler/                        # «Scheduler» Proceso en segundo plano (@Scheduled, CU7 y CU8)
```

---

## 🧩 Patrones de Diseño GoF Implementados

| Patrón | Tipo | Componentes Principales | Propósito |
|---|---|---|---|
| **Builder** | Creacional | `ScrimBuilder` | Construcción paso a paso y validación de invariantes (RN-01 a RN-08). |
| **Abstract Factory** | Creacional | `NotifierFactory`, `DevNotifierFactory`, `ProdNotifierFactory` | Creación de familias de notificadores por canal y entorno. |
| **Adapter** | Estructural | `*Adapter` (`Firebase`, `SendGrid`, `Discord`, `ICal`) | Adaptación de clientes externos al contrato común `Notifier`. |
| **State** | Comportamiento | `ScrimContext`, `ScrimState` (6 estados concretos) | Máquina de estados del ciclo de vida del Scrim (RN-08 a RN-12). |
| **Strategy** | Comportamiento | `MatchmakingStrategy` (`ByMMR`, `ByLatency`, `ByHistory`) | Algoritmos intercambiables de emparejamiento de jugadores. |
| **Command** | Comportamiento | `ScrimCommand`, `ScrimCommandInvoker`, comandos concretos | Acciones reversibles (`execute`/`undo`) en el lobby (RN-12). |
| **Observer** | Comportamiento | `DomainEventBus`, `Subscriber`, `GestorNotificaciones` | Notificación reactiva y desacoplada de eventos de dominio. |
| **Chain of Resp.** | Comportamiento | `ReportHandler`, `AutoResolver`, `BotModerator`, `HumanModerator` | Pipeline escalonado de resolución de reportes y sanciones. |
| **Template Method** | Comportamiento | `ConfiguracionScrimValidator` + 3 validadores concretos | Esqueleto del algoritmo de validación de reglas por juego. |

---

## 🚀 Compilación y Pruebas

Requisitos: **JDK 17+** y **Maven 3.8+**.

```bash
# Compilar el proyecto
mvn compile

# Ejecutar la suite completa de pruebas unitarias, integración y carga
mvn test
```

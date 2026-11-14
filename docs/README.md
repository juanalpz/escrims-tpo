# eScrims – Documentación técnica

eScrims organiza **scrims** (partidas amistosas de eSports: Valorant, LoL, CS2): alguien crea la partida, los jugadores se postulan, se arma el lobby, todos confirman y se juega.

El proyecto es Java 17 + Maven, sin framework. Está pensado para mostrar **patrones de diseño** aplicados a un dominio real.

## Índice

| Doc | Qué explica | Patrón |
|---|---|---|
| [01-entidades.md](01-entidades.md) | Los objetos del dominio (Scrim, Usuario, Equipo…) | – |
| [02-builder.md](02-builder.md) | Cómo se crea un Scrim válido | Builder |
| [03-state.md](03-state.md) | Ciclo de vida del Scrim | State |
| [04-strategy.md](04-strategy.md) | Algoritmos de matchmaking | Strategy |
| [05-lobby-y-suplentes.md](05-lobby-y-suplentes.md) | Equipos A/B y lista de espera | – |
| [06-command.md](06-command.md) | Acciones del organizador con *undo* | Command |
| [07-observer.md](07-observer.md) | Bus de eventos de dominio | Observer |
| [08-notificaciones.md](08-notificaciones.md) | Push / Email / Discord | Abstract Factory + Adapter |
| [09-calendario.md](09-calendario.md) | Sincronización iCal | Adapter + Observer |

## Estructura de paquetes

```
com.escrims
├── domain            ← reglas de negocio, no depende de nada externo
│   ├── entities      Scrim, Usuario, Equipo, enums…
│   ├── builder       ScrimBuilder
│   ├── state         ScrimContext + 6 estados
│   ├── strategy      MatchmakingStrategy + 3 implementaciones
│   ├── command       ScrimCommand + invoker
│   ├── observer      DomainEventBus, eventos
│   ├── notification  Notifier, NotifierFactory, Notificacion
│   └── calendar      CalendarioService, CalendarioSubscriber
└── infrastructure    ← detalles técnicos (red, archivos)
    ├── adapter       Discord, SendGrid, Firebase, iCal
    ├── http          HttpTransport
    └── notification  fábricas Dev / Prod
```

**Regla de oro:** `domain` define *interfaces* (`Notifier`, `CalendarioService`, `HttpTransport`…) e `infrastructure` las implementa. El dominio nunca conoce a Discord ni a SendGrid.

## Reglas de negocio (RN)

El código cita las reglas con su número. Las que aparecen:

| RN | Regla |
|---|---|
| RN-01 | Email verificado para organizar o postularse |
| RN-03 | Rango del jugador dentro del rango del scrim; min ≤ max |
| RN-04 | Latencia máxima del scrim entre 1 y 500 ms |
| RN-05 | Roles obligatorios no pueden superar los cupos |
| RN-06 | Sin sanción/cooldown activo |
| RN-08 | Todo scrim nace en "Buscando jugadores"; al completar cupos se arma el lobby |
| RN-09 | Pasa a Confirmado solo si el 100% confirma |
| RN-10 / RN-17 | Si un titular se baja, el primer suplente (FIFO) lo reemplaza |
| RN-12 | No se pueden modificar equipos/roles una vez que la partida empezó |

## Correr los tests

```bash
mvn test
```

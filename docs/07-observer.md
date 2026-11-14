# 07 · Observer – eventos de dominio

**Paquete:** `domain/observer`

## Idea
Cuando pasa algo importante (un scrim cambia de estado, se promueve un suplente), otras partes del sistema quieren enterarse (notificaciones, calendario) **sin que el dominio las conozca**. Se resuelve con un bus de eventos.

## Piezas

| Clase | Rol |
|---|---|
| `DomainEvent` | Interfaz de todo evento; expone `ocurridoEn()` |
| `Subscriber` | Interfaz del observador: `onEvent(DomainEvent)` |
| `DomainEventBus` | El *subject*: `subscribe`, `unsubscribe`, `publish` |
| `ScrimStateChanged` | Evento: scrim `X` pasó de estado A a estado B |
| `SuplentePromovidoEvent` | Evento: un suplente reemplazó a un titular (RN-17) |

Los eventos son `record`s, o sea inmutables.

## Flujo

```
publisher ──publish(evento)──▶ DomainEventBus ──onEvent──▶ Subscriber 1
                                                └─────────▶ Subscriber 2
```

```java
var bus = new DomainEventBus();
bus.subscribe(new CalendarioSubscriber(calendario, repo::findById));
bus.publish(new ScrimStateChanged(scrim.getId(), "Lobby armado", "Confirmado"));
```

## Detalles
- Es **síncrono**: `publish` llama a cada suscriptor en orden de alta.
- Itera sobre una **copia** de los suscriptores, así un observer puede desuscribirse mientras recibe un evento.
- No admite suscriptores ni eventos `null`.
- Cada suscriptor filtra por tipo de evento (`instanceof`) y ignora los que no le interesan.
- Hoy el único suscriptor concreto es [`CalendarioSubscriber`](09-calendario.md).

> Pendiente: ninguna clase del dominio publica todavía `ScrimStateChanged` ni `SuplentePromovidoEvent`; quien orquesta (un servicio de aplicación) deberá llamar a `bus.publish(...)`.

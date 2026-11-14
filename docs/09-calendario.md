# 09 · Calendario – sincronización iCal

**Dominio:** `domain/calendar` · **Implementación:** `infrastructure/adapter/ICalAdapter`

## Objetivo
Que los scrims confirmados aparezcan en el calendario del jugador (Google Calendar, Outlook, Apple) con un recordatorio.

## Piezas

| Clase | Rol |
|---|---|
| `CalendarioService` | Interfaz (*Target*): `sincronizar(scrim)` y `cancelar(scrim)` |
| `CalendarioSubscriber` | *Observer*: escucha el [bus de eventos](07-observer.md) y llama al servicio |
| `ICalAdapter` | *Adapter*: convierte un `Scrim` a un archivo `.ics` (RFC 5545) |
| `ICalDestino` | Interfaz: dónde se publica el `.ics` |
| `ArchivoICalDestino` | Destino que escribe el `.ics` en una carpeta |

## Flujo

```
ScrimStateChanged ──▶ CalendarioSubscriber ──┬─ nuevo estado "Confirmado" ─▶ calendario.sincronizar(scrim)
                                             └─ nuevo estado "Cancelado"  ─▶ calendario.cancelar(scrim)
                                                (otros estados o scrim inexistente: se ignoran)
```

El subscriber recibe una función `UUID → Optional<Scrim>` para buscar el scrim a partir del id del evento (por ejemplo `repositorio::findById`).

## Qué genera `ICalAdapter`

Un `scrim-<uuid>.ics` con un evento que incluye:
- **Inicio/fin** en UTC (la duración por defecto es 60 min si el scrim no define una).
- **Resumen** (`Scrim VALORANT 5v5`) y descripción con región, modalidad y organizador.
- **Recordatorio** (`VALARM`) N horas antes del inicio.
- **UID** estable por scrim, para que el calendario lo reconozca como el mismo evento.
- **SEQUENCE** que sube con cada publicación: así el cliente reemplaza la versión anterior en vez de duplicar.
- Al cancelar: `METHOD:CANCEL` y `STATUS:CANCELLED`, sin recordatorio.

Además respeta el formato del RFC: escapa `, ; \` y saltos de línea, y pliega las líneas a 75 octetos.

## Uso

```java
var destino = new ArchivoICalDestino(Path.of("calendarios"));
var ical = new ICalAdapter(destino, ZoneId.of("America/Argentina/Buenos_Aires"), 2); // recordar 2 h antes

bus.subscribe(new CalendarioSubscriber(ical, repo::findById));
```

`ICalAdapter.obtenerIcs(scrimId)` devuelve el último `.ics` generado, útil para tests.

## Errores típicos
- Scrim sin `fechaHora` → `IllegalArgumentException` (no se puede agendar).
- Recordatorio menor a 1 hora → `IllegalArgumentException` al construir el adapter.

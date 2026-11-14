# 08 · Notificaciones – Abstract Factory + Adapter

**Dominio:** `domain/notification` · **Implementaciones:** `infrastructure/notification`, `infrastructure/adapter`, `infrastructure/http`

## Objetivo
Enviar mensajes por **Push**, **Email** o **Chat** sin que el dominio sepa qué proveedor hay detrás (Firebase, SendGrid, Discord), y poder cambiar de entorno (dev ↔ prod) sin tocar la lógica.

## Piezas del dominio

- `Notificacion`: mensaje con tipo, `Canal`, destinatario, título y payload. Su `EstadoNotificacion` va `PENDIENTE → ENVIADA` o `FALLIDA`.
- `Canal`: `PUSH`, `EMAIL`, `CHAT`.
- `Notifier`: contrato único: `canal()` y `send(Notificacion)`.
- `NotifierFactory` (**Abstract Factory**): `createPush()`, `createEmail()`, `createChat()` y `create(Canal)`.
- `NotificacionException`: falla de un proveedor.

## Dos "familias" de notifiers

| Fábrica | Push | Email | Chat |
|---|---|---|---|
| `DevNotifierFactory` | `LogNotifier` | `LogNotifier` | `LogNotifier` |
| `ProdNotifierFactory` | `FirebasePushAdapter` | `SendGridAdapter` | `DiscordAdapter` |

- **Dev:** no sale nada a la red. `LogNotifier` loguea y guarda lo "enviado" en memoria (`getEnviadas()`), ideal para tests. Devuelve siempre la misma instancia por canal.
- **Prod:** cada canal usa el adaptador del proveedor real.

## Adapters

Todos extienden `HttpNotifierAdapter`, que resuelve lo común:

1. Verifica que la notificación sea del canal del adaptador.
2. Arma el cuerpo JSON (lo único que cada subclase define).
3. Hace el POST vía `HttpTransport`.
4. Si falla la conexión o el HTTP no es 2xx → marca `FALLIDA` y lanza `NotificacionException`. Si no → `ENVIADA`.

| Adapter | Canal | Destinatario es… |
|---|---|---|
| `DiscordAdapter` | CHAT | una mención (opcional) en el webhook |
| `SendGridAdapter` | EMAIL | la dirección de email |
| `FirebasePushAdapter` | PUSH | el token del dispositivo |

`HttpTransport` es una interfaz mínima (`postJson`); `JavaHttpTransport` la implementa con `java.net.http` y en tests se reemplaza por un fake.

## Configuración de producción
`ProdNotifierConfig.desdeEntorno()` lee variables de entorno:

`DISCORD_WEBHOOK_URL`, `SENDGRID_API_KEY`, `EMAIL_REMITENTE`, `FIREBASE_PROJECT_ID`, `FIREBASE_ACCESS_TOKEN`

Nunca van credenciales en el código, y `toString()` no las expone.

## Uso

```java
NotifierFactory fabrica = new DevNotifierFactory();   // o new ProdNotifierFactory(ProdNotifierConfig.desdeEntorno())
var n = new Notificacion("LOBBY_LISTO", Canal.EMAIL, "ana@mail.com", "Lobby listo", "Confirmá tu asistencia");
fabrica.create(n.getCanal()).send(n);
```

# 03 · State – ciclo de vida del Scrim

**Paquete:** `domain/state`

## Idea
Lo que se puede hacer con un scrim depende de en qué etapa está. En vez de llenar el código de `if (estado == …)`, cada etapa es una clase que decide qué acciones acepta.

- `ScrimState` – interfaz con las 5 acciones: `postular`, `confirmar`, `iniciar`, `finalizar`, `cancelar`.
- 6 estados concretos que la implementan.
- `ScrimContext` – envuelve al `Scrim`, guarda el estado actual y **delega** cada acción en él.

## Diagrama

```
                postular (cupo completo)        todos confirman
  BuscandoState ───────────────────▶ LobbyArmadoState ───────────▶ ConfirmadoState
        │                                   │                           │
        │ cancelar                          │ cancelar                  │ iniciar
        ▼                                   ▼                           ▼
  CanceladoState ◀──────────────────────────┴── cancelar ────────  EnJuegoState
                                                                        │ finalizar
                                                                        ▼
                                                                  FinalizadoState
```
`ConfirmadoState` también puede `cancelar` → `CanceladoState`. `EnJuegoState` **no** se puede cancelar.

## Qué permite cada estado

| Estado | postular | confirmar | iniciar | finalizar | cancelar |
|---|:-:|:-:|:-:|:-:|:-:|
| Buscando jugadores | ✅ | ❌ | ❌ | ❌ | ✅ |
| Lobby armado | ❌ | ✅ | ❌ | ❌ | ✅ |
| Confirmado | ❌ | ❌ | ✅ | ❌ | ✅ |
| En juego | ❌ | ❌ | ❌ | ✅ | ❌ |
| Finalizado | ❌ | ❌ | ❌ | ❌ | ❌ |
| Cancelado | ❌ | ❌ | ❌ | ❌ | ❌ |

Una acción no permitida lanza `IllegalStateException` con un mensaje claro.

## Qué pasa en cada transición

**Buscando → Lobby armado** (`postular`)
Valida: email verificado (RN-01), sin cooldown (RN-06), no postulado antes, rango dentro del límite (RN-03). Si pasa, la postulación se acepta. Cuando las postulaciones aceptadas igualan los cupos, se crea una `Confirmacion` por jugador y se pasa a Lobby armado (RN-08).

**Lobby armado → Confirmado** (`confirmar`)
Cada jugador seleccionado confirma. Solo cuando confirma el 100% se avanza (RN-09).

**Confirmado → En juego → Finalizado**
`iniciar` y `finalizar` son transiciones directas (la fecha/hora de inicio la dispararía un scheduler externo).

## Uso

```java
ScrimContext ctx = new ScrimContext(scrim);
ctx.postular(usuario, RolJuego.DUELISTA);
ctx.confirmar(usuario);
ctx.iniciar();
```
`ScrimContext` mantiene sincronizado `scrim.getEstado()` en cada cambio.

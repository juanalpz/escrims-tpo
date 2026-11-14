# 05 · Lobby y lista de espera

**Clases:** `GestorLobbyScrim`, `Equipo` (en `domain/entities`)

## Idea
Una vez que hay jugadores, hay que repartirlos en dos equipos. `GestorLobbyScrim` crea **Equipo A** y **Equipo B** (tamaño = jugadores por lado del `Formato`) y una **lista de espera** de suplentes.

## Reglas

**Incorporar jugador** (`incorporarJugador`)
1. Si ya está en el lobby o en la espera → error.
2. Si hay lugar en A → entra a A.
3. Si no, si hay lugar en B → entra a B.
4. Si ambos están llenos → va a la lista de espera (devuelve `false`).

**Baja de un titular** (`darDeBajaYPromoverSuplente`, RN-10 / RN-17)
Se saca al titular y, si hay suplentes, entra el **primero de la fila (FIFO)** al mismo equipo con el rol que había pedido. Devuelve el suplente promovido (o vacío si no había).

**Swap** (`intercambiarJugadores`)
Cambia a dos jugadores de equipos distintos, **conservando el rol de cada uno**. Falla si están en el mismo equipo o si alguno no es titular.

**Incorporar a un lado puntual** (`incorporarAEquipo`)
Lo usa la invitación del organizador. Si el jugador estaba en espera, lo saca de ahí.

## Detalles
- La lista de espera es un `LinkedHashMap<Usuario, RolJuego>`: mantiene orden de llegada y no admite duplicados.
- `Equipo` usa un `Set` y un mapa de roles; no acepta pasar del cupo máximo.
- Existe un `SuplentePromovidoEvent` ([07](07-observer.md)) para avisar de una promoción, pero el gestor no lo publica por sí mismo.

Las acciones manuales del organizador sobre estos equipos se hacen con [Commands](06-command.md).

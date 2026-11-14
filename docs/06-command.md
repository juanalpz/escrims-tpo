# 06 · Command – acciones con undo

**Paquete:** `domain/command`

## Idea
Antes de que empiece la partida, el organizador puede mover jugadores y roles. Cada acción es un objeto que sabe **ejecutarse** y **deshacerse**.

```java
public interface ScrimCommand {
    void execute();
    void undo();
}
```

## Los comandos

| Comando | `execute` | `undo` |
|---|---|---|
| `InvitarJugadorCommand` | Mete al jugador en el equipo/lado indicado con un rol | Lo saca del equipo |
| `AsignarRolCommand` | Cambia el rol del jugador (recuerda el anterior) | Restaura el rol anterior |
| `SwapJugadoresCommand` | Intercambia dos jugadores entre equipos | Vuelve a intercambiarlos |

Todos operan sobre un `GestorLobbyScrim` ([05](05-lobby-y-suplentes.md)).

## El invoker

`ScrimCommandInvoker` ejecuta comandos y los apila en un historial **LIFO**:

```java
var invoker = new ScrimCommandInvoker();
invoker.ejecutar(new InvitarJugadorCommand(gestor, jugador, RolJuego.ADC, LadoEquipo.EQUIPO_A));
invoker.ejecutar(new AsignarRolCommand(gestor, jugador, RolJuego.MID));

invoker.deshacerUltimo(); // vuelve el rol anterior
invoker.deshacerUltimo(); // saca al jugador
invoker.deshacerUltimo(); // false: no hay nada que deshacer
```

## Restricciones
- **RN-12:** si el scrim está `EnJuego` o `Finalizado`, tanto `execute` como `undo` fallan.
- No se puede deshacer un comando que no se ejecutó.
- Si `execute` falla, el comando **no** entra al historial.

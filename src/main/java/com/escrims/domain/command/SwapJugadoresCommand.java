package com.escrims.domain.command;

import com.escrims.domain.entities.GestorLobbyScrim;
import com.escrims.domain.entities.Usuario;
import com.escrims.domain.state.EnJuegoState;
import com.escrims.domain.state.FinalizadoState;

public class SwapJugadoresCommand implements ScrimCommand {

    private final GestorLobbyScrim gestorLobby;
    private final Usuario jugador1;
    private final Usuario jugador2;
    private boolean ejecutado = false;

    public SwapJugadoresCommand(GestorLobbyScrim gestorLobby, Usuario jugador1, Usuario jugador2) {
        if (gestorLobby == null || jugador1 == null || jugador2 == null) {
            throw new IllegalArgumentException("Gestor y ambos jugadores son obligatorios para el swap.");
        }
        this.gestorLobby = gestorLobby;
        this.jugador1 = jugador1;
        this.jugador2 = jugador2;
    }

    @Override
    public void execute() {
        validarEstadoModificable();
        gestorLobby.intercambiarJugadores(jugador1, jugador2);
        this.ejecutado = true;
    }

    @Override
    public void undo() {
        if (!ejecutado) {
            throw new IllegalStateException("No se puede deshacer un intercambio que aún no fue ejecutado.");
        }
        validarEstadoModificable();
        gestorLobby.intercambiarJugadores(jugador1, jugador2);
        this.ejecutado = false;
    }

    private void validarEstadoModificable() {
        var estado = gestorLobby.getScrim().getEstado();
        if (estado instanceof EnJuegoState || estado instanceof FinalizadoState) {
            throw new IllegalStateException("No se pueden intercambiar jugadores una vez iniciada la partida (RN-12).");
        }
    }
}

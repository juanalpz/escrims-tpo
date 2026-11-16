package com.escrims.domain.command;

import com.escrims.domain.entities.GestorLobbyScrim;
import com.escrims.domain.entities.Usuario;
import com.escrims.domain.state.EnJuegoState;
import com.escrims.domain.state.FinalizadoState;

/**
 * Comando concreto que intercambia las posiciones o roles de dos jugadores en el lobby de forma reversible (Patrón Command, RN-12).
 */
public class SwapJugadoresCommand implements ScrimCommand {

    private final GestorLobbyScrim gestorLobby;
    private final Usuario jugador1;
    private final Usuario jugador2;
    private boolean ejecutado = false;

    /**
     * Inicializa el comando con el gestor del lobby y los dos jugadores a intercambiar.
     *
     * @param gestorLobby gestor del lobby que administra los equipos del scrim.
     * @param jugador1    primer jugador involucrado en el intercambio.
     * @param jugador2    segundo jugador involucrado en el intercambio.
     * @throws IllegalArgumentException si el gestor o alguno de los jugadores es nulo.
     */
    public SwapJugadoresCommand(GestorLobbyScrim gestorLobby, Usuario jugador1, Usuario jugador2) {
        if (gestorLobby == null || jugador1 == null || jugador2 == null) {
            throw new IllegalArgumentException("Gestor y ambos jugadores son obligatorios para el swap.");
        }
        this.gestorLobby = gestorLobby;
        this.jugador1 = jugador1;
        this.jugador2 = jugador2;
    }

    /**
     * Ejecuta el intercambio entre ambos jugadores dentro del lobby antes del inicio de la partida.
     *
     * @throws IllegalStateException si la partida ya inició o finalizó (RN-12).
     */
    @Override
    public void execute() {
        validarEstadoModificable();
        gestorLobby.intercambiarJugadores(jugador1, jugador2);
        this.ejecutado = true;
    }

    /**
     * Revierte el intercambio volviendo a permutar a los mismos dos jugadores a sus posiciones originales.
     *
     * @throws IllegalStateException si el comando no fue ejecutado o la partida ya inició/finalizó (RN-12).
     */
    @Override
    public void undo() {
        if (!ejecutado) {
            throw new IllegalStateException("No se puede deshacer un intercambio que aún no fue ejecutado.");
        }
        validarEstadoModificable();
        gestorLobby.intercambiarJugadores(jugador1, jugador2);
        this.ejecutado = false;
    }

    /**
     * Verifica que el scrim no haya comenzado ni finalizado antes de intercambiar jugadores (RN-12).
     *
     * @throws IllegalStateException si el estado actual es {@link EnJuegoState} o {@link FinalizadoState}.
     */
    private void validarEstadoModificable() {
        var estado = gestorLobby.getScrim().getEstado();
        if (estado instanceof EnJuegoState || estado instanceof FinalizadoState) {
            throw new IllegalStateException("No se pueden intercambiar jugadores una vez iniciada la partida (RN-12).");
        }
    }
}

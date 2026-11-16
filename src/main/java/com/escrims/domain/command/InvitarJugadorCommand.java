package com.escrims.domain.command;

import com.escrims.domain.entities.Equipo;
import com.escrims.domain.entities.GestorLobbyScrim;
import com.escrims.domain.entities.LadoEquipo;
import com.escrims.domain.entities.RolJuego;
import com.escrims.domain.entities.Usuario;
import com.escrims.domain.state.EnJuegoState;
import com.escrims.domain.state.FinalizadoState;

/**
 * Comando concreto que incorpora directamente a un jugador invitado en un equipo del lobby y permite revertir la acción (Patrón Command, RN-12).
 */
public class InvitarJugadorCommand implements ScrimCommand {

    private final GestorLobbyScrim gestorLobby;
    private final Usuario jugadorInvitado;
    private final RolJuego rolAsignado;
    private final LadoEquipo ladoDestino;
    private boolean ejecutado = false;

    /**
     * Inicializa el comando con el gestor del lobby, el jugador invitado, su rol y el equipo de destino.
     *
     * @param gestorLobby     gestor del lobby que administra los equipos del scrim.
     * @param jugadorInvitado usuario invitado que será incorporado al encuentro.
     * @param rolAsignado     rol dentro del juego asignado al jugador invitado.
     * @param ladoDestino     equipo de destino (Equipo A o Equipo B).
     * @throws IllegalArgumentException si alguno de los parámetros recibidos es nulo.
     */
    public InvitarJugadorCommand(GestorLobbyScrim gestorLobby, Usuario jugadorInvitado, RolJuego rolAsignado,
            LadoEquipo ladoDestino) {
        if (gestorLobby == null || jugadorInvitado == null || rolAsignado == null || ladoDestino == null) {
            throw new IllegalArgumentException("Todos los parámetros de invitación son obligatorios.");
        }
        this.gestorLobby = gestorLobby;
        this.jugadorInvitado = jugadorInvitado;
        this.rolAsignado = rolAsignado;
        this.ladoDestino = ladoDestino;
    }

    /**
     * Incorpora al jugador invitado en el equipo y rol especificados antes del inicio de la partida.
     *
     * @throws IllegalStateException si la partida ya inició/finalizó (RN-12) o no hay cupo disponible en el equipo de destino.
     */
    @Override
    public void execute() {
        validarEstadoModificable();
        boolean agregado = gestorLobby.incorporarAEquipo(jugadorInvitado, rolAsignado, ladoDestino);
        if (!agregado) {
            throw new IllegalStateException("No se pudo incorporar al jugador invitado en " + ladoDestino);
        }
        this.ejecutado = true;
    }

    /**
     * Revierte la invitación removiendo al jugador previamente incorporado del equipo de destino.
     *
     * @throws IllegalStateException si el comando no fue ejecutado o la partida ya inició/finalizó (RN-12).
     */
    @Override
    public void undo() {
        if (!ejecutado) {
            throw new IllegalStateException("No se puede deshacer una invitación no ejecutada.");
        }
        validarEstadoModificable();
        Equipo equipo = (ladoDestino == LadoEquipo.EQUIPO_A) ? gestorLobby.getEquipoA() : gestorLobby.getEquipoB();
        equipo.removerJugador(jugadorInvitado);
        this.ejecutado = false;
    }

    /**
     * Verifica que el scrim no haya comenzado ni finalizado antes de modificar los equipos (RN-12).
     *
     * @throws IllegalStateException si el estado actual es {@link EnJuegoState} o {@link FinalizadoState}.
     */
    private void validarEstadoModificable() {
        var estado = gestorLobby.getScrim().getEstado();
        if (estado instanceof EnJuegoState || estado instanceof FinalizadoState) {
            throw new IllegalStateException(
                    "No se pueden invitar o remover jugadores una vez iniciada la partida (RN-12).");
        }
    }
}

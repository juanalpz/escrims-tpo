package com.escrims.domain.command;

import com.escrims.domain.entities.Equipo;
import com.escrims.domain.entities.GestorLobbyScrim;
import com.escrims.domain.entities.LadoEquipo;
import com.escrims.domain.entities.RolJuego;
import com.escrims.domain.entities.Usuario;
import com.escrims.domain.state.EnJuegoState;
import com.escrims.domain.state.FinalizadoState;

public class InvitarJugadorCommand implements ScrimCommand {

    private final GestorLobbyScrim gestorLobby;
    private final Usuario jugadorInvitado;
    private final RolJuego rolAsignado;
    private final LadoEquipo ladoDestino;
    private boolean ejecutado = false;

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

    @Override
    public void execute() {
        validarEstadoModificable();
        boolean agregado = gestorLobby.incorporarAEquipo(jugadorInvitado, rolAsignado, ladoDestino);
        if (!agregado) {
            throw new IllegalStateException("No se pudo incorporar al jugador invitado en " + ladoDestino);
        }
        this.ejecutado = true;
    }

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

    private void validarEstadoModificable() {
        var estado = gestorLobby.getScrim().getEstado();
        if (estado instanceof EnJuegoState || estado instanceof FinalizadoState) {
            throw new IllegalStateException(
                    "No se pueden invitar o remover jugadores una vez iniciada la partida (RN-12).");
        }
    }
}

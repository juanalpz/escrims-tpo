package com.escrims.domain.command;

import com.escrims.domain.entities.Equipo;
import com.escrims.domain.entities.GestorLobbyScrim;
import com.escrims.domain.entities.RolJuego;
import com.escrims.domain.entities.Usuario;
import com.escrims.domain.state.EnJuegoState;
import com.escrims.domain.state.FinalizadoState;

public class AsignarRolCommand implements ScrimCommand {

    private final GestorLobbyScrim gestorLobby;
    private final Usuario jugador;
    private final RolJuego nuevoRol;
    private RolJuego rolAnterior;
    private boolean ejecutado = false;

    public AsignarRolCommand(GestorLobbyScrim gestorLobby, Usuario jugador, RolJuego nuevoRol) {
        if (gestorLobby == null || jugador == null || nuevoRol == null) {
            throw new IllegalArgumentException("Gestor, jugador y nuevo rol son obligatorios.");
        }
        this.gestorLobby = gestorLobby;
        this.jugador = jugador;
        this.nuevoRol = nuevoRol;
    }

    @Override
    public void execute() {
        validarEstadoModificable();
        Equipo equipo = gestorLobby.obtenerEquipoDe(jugador)
                .orElseThrow(() -> new IllegalStateException("El jugador no forma parte de ningún equipo del scrim."));

        this.rolAnterior = equipo.getRolDe(jugador);
        equipo.asignarRol(jugador, nuevoRol);
        this.ejecutado = true;
    }

    @Override
    public void undo() {
        if (!ejecutado) {
            throw new IllegalStateException("No se puede deshacer un comando que aún no fue ejecutado.");
        }
        validarEstadoModificable();
        Equipo equipo = gestorLobby.obtenerEquipoDe(jugador)
                .orElseThrow(() -> new IllegalStateException("El jugador ya no pertenece al equipo."));

        equipo.asignarRol(jugador, rolAnterior);
        this.ejecutado = false;
    }

    private void validarEstadoModificable() {
        var estado = gestorLobby.getScrim().getEstado();
        if (estado instanceof EnJuegoState || estado instanceof FinalizadoState) {
            throw new IllegalStateException("No se pueden modificar roles una vez iniciada la partida (RN-12).");
        }
    }
}

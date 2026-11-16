package com.escrims.domain.command;

import com.escrims.domain.entities.Equipo;
import com.escrims.domain.entities.GestorLobbyScrim;
import com.escrims.domain.entities.RolJuego;
import com.escrims.domain.entities.Usuario;
import com.escrims.domain.state.EnJuegoState;
import com.escrims.domain.state.FinalizadoState;

/**
 * Comando concreto que reasigna el rol de un jugador en su equipo y permite deshacer el cambio (Patrón Command, RN-12).
 */
public class AsignarRolCommand implements ScrimCommand {

    private final GestorLobbyScrim gestorLobby;
    private final Usuario jugador;
    private final RolJuego nuevoRol;
    private RolJuego rolAnterior;
    private boolean ejecutado = false;

    /**
     * Inicializa el comando con el gestor del lobby, el jugador objetivo y el nuevo rol a asignar.
     *
     * @param gestorLobby gestor del lobby que contiene los equipos del scrim.
     * @param jugador     jugador al que se le modificará el rol.
     * @param nuevoRol    nuevo rol dentro del juego a asignar.
     * @throws IllegalArgumentException si algún parámetro recibido es nulo.
     */
    public AsignarRolCommand(GestorLobbyScrim gestorLobby, Usuario jugador, RolJuego nuevoRol) {
        if (gestorLobby == null || jugador == null || nuevoRol == null) {
            throw new IllegalArgumentException("Gestor, jugador y nuevo rol son obligatorios.");
        }
        this.gestorLobby = gestorLobby;
        this.jugador = jugador;
        this.nuevoRol = nuevoRol;
    }

    /**
     * Ejecuta la reasignación de rol guardando el rol previo para permitir su reversión.
     *
     * @throws IllegalStateException si la partida ya inició/finalizó (RN-12) o el jugador no está en un equipo.
     */
    @Override
    public void execute() {
        validarEstadoModificable();
        Equipo equipo = gestorLobby.obtenerEquipoDe(jugador)
                .orElseThrow(() -> new IllegalStateException("El jugador no forma parte de ningún equipo del scrim."));

        this.rolAnterior = equipo.getRolDe(jugador);
        equipo.asignarRol(jugador, nuevoRol);
        this.ejecutado = true;
    }

    /**
     * Revierte el cambio restaurando el rol que tenía el jugador antes de ejecutar el comando.
     *
     * @throws IllegalStateException si el comando no fue ejecutado, la partida ya inició (RN-12) o el jugador ya no está en el equipo.
     */
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

    /**
     * Verifica que el scrim no haya comenzado ni finalizado antes de modificar roles (RN-12).
     *
     * @throws IllegalStateException si el estado actual es {@link EnJuegoState} o {@link FinalizadoState}.
     */
    private void validarEstadoModificable() {
        var estado = gestorLobby.getScrim().getEstado();
        if (estado instanceof EnJuegoState || estado instanceof FinalizadoState) {
            throw new IllegalStateException("No se pueden modificar roles una vez iniciada la partida (RN-12).");
        }
    }
}

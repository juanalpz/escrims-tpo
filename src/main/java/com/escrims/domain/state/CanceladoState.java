package com.escrims.domain.state;

import com.escrims.domain.entities.RolJuego;
import com.escrims.domain.entities.Usuario;

/**
 * Implementa el ConcreteState del patrón State correspondiente a un scrim
 * cancelado, impidiendo nuevas operaciones y transiciones de estado.
 */
public class CanceladoState implements ScrimState {

    /**
     * Impide nuevas postulaciones a un scrim cancelado.
     *
     * @param ctx contexto que administra el estado del scrim
     * @param usuario jugador que intenta postularse
     * @param rol rol de juego solicitado
     * @throws IllegalStateException siempre, porque el scrim está cancelado
     */
    @Override
    public void postular(ScrimContext ctx, Usuario usuario, RolJuego rol) {
        throw new IllegalStateException("El scrim fue cancelado.");
    }

    /**
     * Impide confirmar la participación en un scrim cancelado.
     *
     * @param ctx contexto que administra el estado del scrim
     * @param usuario jugador que intenta confirmar su participación
     * @throws IllegalStateException siempre, porque el scrim está cancelado
     */
    @Override
    public void confirmar(ScrimContext ctx, Usuario usuario) {
        throw new IllegalStateException("El scrim fue cancelado.");
    }

    /**
     * Impide iniciar una partida cuyo scrim fue cancelado.
     *
     * @param ctx contexto que administra el estado del scrim
     * @throws IllegalStateException siempre, porque el scrim está cancelado
     */
    @Override
    public void iniciar(ScrimContext ctx) {
        throw new IllegalStateException("No se puede iniciar un scrim cancelado.");
    }

    /**
     * Impide finalizar una partida cuyo scrim fue cancelado.
     *
     * @param ctx contexto que administra el estado del scrim
     * @throws IllegalStateException siempre, porque el scrim está cancelado
     */
    @Override
    public void finalizar(ScrimContext ctx) {
        throw new IllegalStateException("No se puede finalizar un scrim cancelado.");
    }

    /**
     * Impide cancelar nuevamente un scrim que ya se encuentra cancelado.
     *
     * @param ctx contexto que administra el estado del scrim
     * @throws IllegalStateException siempre, porque el scrim ya está cancelado
     */
    @Override
    public void cancelar(ScrimContext ctx) {
        throw new IllegalStateException("El scrim ya se encuentra cancelado.");
    }

    /**
     * Obtiene el nombre descriptivo del estado actual.
     *
     * @return nombre del estado cancelado
     */
    @Override
    public String getNombreEstado() {
        return "Cancelado";
    }
}

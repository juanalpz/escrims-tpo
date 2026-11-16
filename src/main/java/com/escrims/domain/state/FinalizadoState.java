package com.escrims.domain.state;

import com.escrims.domain.entities.RolJuego;
import com.escrims.domain.entities.Usuario;

/**
 * Implementa el ConcreteState del patrón State correspondiente a un scrim
 * finalizado, impidiendo nuevas operaciones y transiciones de estado.
 */
public class FinalizadoState implements ScrimState {

    /**
     * Impide nuevas postulaciones a un scrim finalizado.
     *
     * @param ctx contexto que administra el estado del scrim
     * @param usuario jugador que intenta postularse
     * @param rol rol de juego solicitado
     * @throws IllegalStateException siempre, porque el scrim ya finalizó
     */
    @Override
    public void postular(ScrimContext ctx, Usuario usuario, RolJuego rol) {
        throw new IllegalStateException("El scrim ya ha finalizado.");
    }

    /**
     * Impide confirmar la participación en un scrim finalizado.
     *
     * @param ctx contexto que administra el estado del scrim
     * @param usuario jugador que intenta confirmar su participación
     * @throws IllegalStateException siempre, porque el scrim ya finalizó
     */
    @Override
    public void confirmar(ScrimContext ctx, Usuario usuario) {
        throw new IllegalStateException("El scrim ya ha finalizado.");
    }

    /**
     * Impide iniciar nuevamente una partida finalizada.
     *
     * @param ctx contexto que administra el estado del scrim
     * @throws IllegalStateException siempre, porque la partida ya finalizó
     */
    @Override
    public void iniciar(ScrimContext ctx) {
        throw new IllegalStateException("El scrim ya ha finalizado.");
    }

    /**
     * Impide finalizar nuevamente un scrim que ya se encuentra finalizado.
     *
     * @param ctx contexto que administra el estado del scrim
     * @throws IllegalStateException siempre, porque el scrim ya está finalizado
     */
    @Override
    public void finalizar(ScrimContext ctx) {
        throw new IllegalStateException("El scrim ya se encuentra finalizado.");
    }

    /**
     * Impide cancelar un scrim después de la finalización de la partida (RN-12).
     *
     * @param ctx contexto que administra el estado del scrim
     * @throws IllegalStateException siempre, porque el scrim ya finalizó
     */
    @Override
    public void cancelar(ScrimContext ctx) {
        throw new IllegalStateException("No se puede cancelar un scrim finalizado.");
    }

    /**
     * Obtiene el nombre descriptivo del estado actual.
     *
     * @return nombre del estado finalizado
     */
    @Override
    public String getNombreEstado() {
        return "Finalizado";
    }
}

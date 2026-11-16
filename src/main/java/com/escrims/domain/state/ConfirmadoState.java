package com.escrims.domain.state;

import com.escrims.domain.entities.RolJuego;
import com.escrims.domain.entities.Usuario;

/**
 * Implementa el ConcreteState del patrón State correspondiente a un scrim
 * con todos sus jugadores confirmados y listo para iniciar (RN-09).
 */
public class ConfirmadoState implements ScrimState {

    /**
     * Impide nuevas postulaciones a un scrim confirmado.
     *
     * @param ctx contexto que administra el estado del scrim
     * @param usuario jugador que intenta postularse
     * @param rol rol de juego solicitado
     * @throws IllegalStateException siempre, porque el scrim está confirmado y cerrado
     */
    @Override
    public void postular(ScrimContext ctx, Usuario usuario, RolJuego rol) {
        throw new IllegalStateException("El scrim ya está confirmado y cerrado.");
    }

    /**
     * Impide registrar nuevas confirmaciones cuando todos los jugadores
     * ya confirmaron su participación.
     *
     * @param ctx contexto que administra el estado del scrim
     * @param usuario jugador que intenta confirmar su participación
     * @throws IllegalStateException siempre, porque todas las confirmaciones están completas
     */
    @Override
    public void confirmar(ScrimContext ctx, Usuario usuario) {
        throw new IllegalStateException("Todos los jugadores ya confirmaron su participación.");
    }

    /**
     * Inicia la partida mediante la transición al estado EnJuego (RN-09).
     *
     * @param ctx contexto que administra el estado del scrim
     */
    @Override
    public void iniciar(ScrimContext ctx) {
        ctx.setState(new EnJuegoState());
    }

    /**
     * Impide finalizar un scrim que todavía no inició su partida.
     *
     * @param ctx contexto que administra el estado del scrim
     * @throws IllegalStateException siempre, porque el scrim debe pasar
     *         primero al estado EnJuego
     */
    @Override
    public void finalizar(ScrimContext ctx) {
        throw new IllegalStateException("El scrim debe pasar primero a En juego antes de finalizar.");
    }

    /**
     * Cancela el scrim confirmado antes del inicio de la partida (RN-12).
     *
     * @param ctx contexto que administra el estado del scrim
     */
    @Override
    public void cancelar(ScrimContext ctx) {
        ctx.setState(new CanceladoState());
    }

    /**
     * Obtiene el nombre descriptivo del estado actual.
     *
     * @return nombre del estado confirmado
     */
    @Override
    public String getNombreEstado() {
        return "Confirmado";
    }
}

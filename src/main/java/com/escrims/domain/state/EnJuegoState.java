package com.escrims.domain.state;

import com.escrims.domain.entities.RolJuego;
import com.escrims.domain.entities.Usuario;

/**
 * Implementa el ConcreteState del patrón State correspondiente a una partida
 * en curso, restringiendo nuevas participaciones y cancelaciones (RN-12).
 */
public class EnJuegoState implements ScrimState {

    /**
     * Impide nuevas postulaciones mientras la partida está en curso.
     *
     * @param ctx contexto que administra el estado del scrim
     * @param usuario jugador que intenta postularse
     * @param rol rol de juego solicitado
     * @throws IllegalStateException siempre, porque la partida ya comenzó
     */
    @Override
    public void postular(ScrimContext ctx, Usuario usuario, RolJuego rol) {
        throw new IllegalStateException("La partida ya está en curso.");
    }

    /**
     * Impide confirmar la participación una vez iniciada la partida.
     *
     * @param ctx contexto que administra el estado del scrim
     * @param usuario jugador que intenta confirmar su participación
     * @throws IllegalStateException siempre, porque la partida ya comenzó
     */
    @Override
    public void confirmar(ScrimContext ctx, Usuario usuario) {
        throw new IllegalStateException("La partida ya está en curso.");
    }

    /**
     * Impide iniciar nuevamente una partida que ya está en curso.
     *
     * @param ctx contexto que administra el estado del scrim
     * @throws IllegalStateException siempre, porque la partida ya fue iniciada
     */
    @Override
    public void iniciar(ScrimContext ctx) {
        throw new IllegalStateException("La partida ya se encuentra en juego.");
    }

    /**
     * Finaliza la partida mediante la transición al estado Finalizado.
     *
     * @param ctx contexto que administra el estado del scrim
     */
    @Override
    public void finalizar(ScrimContext ctx) {
        ctx.setState(new FinalizadoState());
    }

    /**
     * Impide cancelar un scrim una vez iniciada la partida (RN-12).
     *
     * @param ctx contexto que administra el estado del scrim
     * @throws IllegalStateException siempre, porque la partida está en curso
     */
    @Override
    public void cancelar(ScrimContext ctx) {
        // RN-12: no se permite cancelar un scrim una vez iniciada la partida.
        throw new IllegalStateException("No se puede cancelar un scrim que ya está en juego.");
    }

    /**
     * Obtiene el nombre descriptivo del estado actual.
     *
     * @return nombre del estado en juego
     */
    @Override
    public String getNombreEstado() {
        return "En juego";
    }
}

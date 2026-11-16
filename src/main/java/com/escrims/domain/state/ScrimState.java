package com.escrims.domain.state;

import com.escrims.domain.entities.RolJuego;
import com.escrims.domain.entities.Usuario;

/**
 * Define el contrato State del patrón State para gestionar las operaciones
 * permitidas en cada fase del ciclo de vida de un scrim (RN-08 a RN-12).
 */
public interface ScrimState {

    /**
     * Procesa la postulación de un jugador según el estado actual del scrim.
     *
     * @param ctx contexto que administra el estado del scrim
     * @param usuario jugador que solicita incorporarse
     * @param rol rol de juego solicitado
     * @throws IllegalArgumentException si los datos de postulación son inválidos
     * @throws IllegalStateException si el estado actual no permite postularse
     *         o se incumplen los requisitos de participación
     */
    void postular(ScrimContext ctx, Usuario usuario, RolJuego rol);

    /**
     * Procesa la confirmación de participación de un jugador
     * seleccionado para el lobby (RN-09).
     *
     * @param ctx contexto que administra el estado del scrim
     * @param usuario jugador que desea confirmar su participación
     * @throws IllegalArgumentException si el usuario es nulo
     *         durante la confirmación en LobbyArmadoState
     * @throws IllegalStateException si el estado actual no permite confirmar
     *         o el jugador no cumple las condiciones necesarias
     */
    void confirmar(ScrimContext ctx, Usuario usuario);

    /**
     * Inicia la partida cuando el estado actual permite realizar
     * la transición a EnJuegoState.
     *
     * @param ctx contexto que administra el estado del scrim
     * @throws IllegalStateException si el estado actual no permite
     *         iniciar la partida
     */
    void iniciar(ScrimContext ctx);

    /**
     * Finaliza la partida cuando se encuentra en estado EnJuegoState.
     *
     * @param ctx contexto que administra el estado del scrim
     * @throws IllegalStateException si el estado actual no permite
     *         finalizar la partida
     */
    void finalizar(ScrimContext ctx);

    /**
     * Cancela el scrim antes del inicio de la partida (RN-12).
     *
     * @param ctx contexto que administra el estado del scrim
     * @throws IllegalStateException si el estado actual no permite
     *         cancelar la partida
     */
    void cancelar(ScrimContext ctx);

    /**
     * Obtiene el nombre descriptivo del estado actual del scrim.
     *
     * @return nombre legible del estado
     */
    String getNombreEstado();
}

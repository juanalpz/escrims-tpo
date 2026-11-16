package com.escrims.domain.state;

import com.escrims.domain.entities.*;

/**
 * Implementa el ConcreteState del patrón State correspondiente a un scrim
 * con el lobby completo, pendiente de confirmación de los jugadores (RN-09).
 */
public class LobbyArmadoState implements ScrimState {

    /**
     * Impide nuevas postulaciones cuando el lobby ya está completo.
     *
     * @param ctx contexto que administra el estado del scrim
     * @param usuario jugador que intenta postularse
     * @param rol rol de juego solicitado
     * @throws IllegalStateException siempre, porque los cupos están completos
     */
    @Override
    public void postular(ScrimContext ctx, Usuario usuario, RolJuego rol) {
        throw new IllegalStateException("El lobby ya está armado y los cupos están completos.");
    }

    /**
     * Registra la confirmación de un jugador seleccionado y realiza
     * la transición al estado Confirmado cuando todos confirman (RN-09).
     *
     * @param ctx contexto que administra el estado del scrim
     * @param usuario jugador que confirma su participación
     * @throws IllegalArgumentException si el usuario es nulo
     * @throws IllegalStateException si el usuario no pertenece al lobby
     *         o si su confirmación no admite la operación
     */
    @Override
    public void confirmar(ScrimContext ctx, Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario es obligatorio para confirmar.");
        }
        Scrim scrim = ctx.getScrim();

        Confirmacion confirmacion = scrim.getConfirmaciones().stream()
                .filter(c -> c.getUsuario().equals(usuario))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "El usuario no pertenece a los jugadores seleccionados en este lobby."));

        confirmacion.confirmar();

        // RN-09: la transición requiere la confirmación del 100% de los participantes.
        if (ctx.todosConfirmaron()) {
            ctx.setState(new ConfirmadoState());
        }
    }

    /**
     * Impide iniciar la partida mientras existan jugadores sin confirmar.
     *
     * @param ctx contexto que administra el estado del scrim
     * @throws IllegalStateException siempre, porque el scrim aún no está confirmado
     */
    @Override
    public void iniciar(ScrimContext ctx) {
        throw new IllegalStateException("No se puede iniciar hasta que el 100% de los jugadores confirme asistencia.");
    }

    /**
     * Impide finalizar una partida que todavía no comenzó.
     *
     * @param ctx contexto que administra el estado del scrim
     * @throws IllegalStateException siempre, porque el scrim aún no fue iniciado
     */
    @Override
    public void finalizar(ScrimContext ctx) {
        throw new IllegalStateException("El scrim aún no ha comenzado.");
    }

    /**
     * Cancela el scrim antes del inicio de la partida,
     * realizando la transición al estado Cancelado (RN-12).
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
     * @return nombre del estado lobby armado
     */
    @Override
    public String getNombreEstado() {
        return "Lobby armado";
    }
}

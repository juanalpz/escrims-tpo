package com.escrims.domain.state;

import com.escrims.domain.entities.*;

public class LobbyArmadoState implements ScrimState {

    @Override
    public void postular(ScrimContext ctx, Usuario usuario, RolJuego rol) {
        throw new IllegalStateException("El lobby ya está armado y los cupos están completos.");
    }

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

        // RN-09: Solo pasa a Confirmado si el 100% de los participantes confirma[cite:
        // 5, 7]
        if (ctx.todosConfirmaron()) {
            ctx.setState(new ConfirmadoState());
        }
    }

    @Override
    public void iniciar(ScrimContext ctx) {
        throw new IllegalStateException("No se puede iniciar hasta que el 100% de los jugadores confirme asistencia.");
    }

    @Override
    public void finalizar(ScrimContext ctx) {
        throw new IllegalStateException("El scrim aún no ha comenzado.");
    }

    @Override
    public void cancelar(ScrimContext ctx) {
        ctx.setState(new CanceladoState());
    }

    @Override
    public String getNombreEstado() {
        return "Lobby armado";
    }
}
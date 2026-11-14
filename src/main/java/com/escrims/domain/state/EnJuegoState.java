package com.escrims.domain.state;

import com.escrims.domain.entities.RolJuego;
import com.escrims.domain.entities.Usuario;

public class EnJuegoState implements ScrimState {

    @Override
    public void postular(ScrimContext ctx, Usuario usuario, RolJuego rol) {
        throw new IllegalStateException("La partida ya está en curso.");
    }

    @Override
    public void confirmar(ScrimContext ctx, Usuario usuario) {
        throw new IllegalStateException("La partida ya está en curso.");
    }

    @Override
    public void iniciar(ScrimContext ctx) {
        throw new IllegalStateException("La partida ya se encuentra en juego.");
    }

    @Override
    public void finalizar(ScrimContext ctx) {
        ctx.setState(new FinalizadoState());
    }

    @Override
    public void cancelar(ScrimContext ctx) {
        // Invariante: No se puede cancelar una vez que pasó a En juego[cite: 5, 7]
        throw new IllegalStateException("No se puede cancelar un scrim que ya está en juego.");
    }

    @Override
    public String getNombreEstado() {
        return "En juego";
    }
}

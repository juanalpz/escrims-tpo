package com.escrims.domain.state;

import com.escrims.domain.entities.RolJuego;
import com.escrims.domain.entities.Usuario;

public class CanceladoState implements ScrimState {

    @Override
    public void postular(ScrimContext ctx, Usuario usuario, RolJuego rol) {
        throw new IllegalStateException("El scrim fue cancelado.");
    }

    @Override
    public void confirmar(ScrimContext ctx, Usuario usuario) {
        throw new IllegalStateException("El scrim fue cancelado.");
    }

    @Override
    public void iniciar(ScrimContext ctx) {
        throw new IllegalStateException("No se puede iniciar un scrim cancelado.");
    }

    @Override
    public void finalizar(ScrimContext ctx) {
        throw new IllegalStateException("No se puede finalizar un scrim cancelado.");
    }

    @Override
    public void cancelar(ScrimContext ctx) {
        throw new IllegalStateException("El scrim ya se encuentra cancelado.");
    }

    @Override
    public String getNombreEstado() {
        return "Cancelado";
    }
}

package com.escrims.domain.state;

import com.escrims.domain.entities.RolJuego;
import com.escrims.domain.entities.Usuario;

public class FinalizadoState implements ScrimState {

    @Override
    public void postular(ScrimContext ctx, Usuario usuario, RolJuego rol) {
        throw new IllegalStateException("El scrim ya ha finalizado.");
    }

    @Override
    public void confirmar(ScrimContext ctx, Usuario usuario) {
        throw new IllegalStateException("El scrim ya ha finalizado.");
    }

    @Override
    public void iniciar(ScrimContext ctx) {
        throw new IllegalStateException("El scrim ya ha finalizado.");
    }

    @Override
    public void finalizar(ScrimContext ctx) {
        throw new IllegalStateException("El scrim ya se encuentra finalizado.");
    }

    @Override
    public void cancelar(ScrimContext ctx) {
        throw new IllegalStateException("No se puede cancelar un scrim finalizado.");
    }

    @Override
    public String getNombreEstado() {
        return "Finalizado";
    }
}
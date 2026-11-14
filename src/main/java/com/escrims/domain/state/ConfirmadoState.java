package com.escrims.domain.state;

import com.escrims.domain.entities.RolJuego;
import com.escrims.domain.entities.Usuario;

public class ConfirmadoState implements ScrimState {

    @Override
    public void postular(ScrimContext ctx, Usuario usuario, RolJuego rol) {
        throw new IllegalStateException("El scrim ya está confirmado y cerrado.");
    }

    @Override
    public void confirmar(ScrimContext ctx, Usuario usuario) {
        throw new IllegalStateException("Todos los jugadores ya confirmaron su participación.");
    }

    @Override
    public void iniciar(ScrimContext ctx) {
        ctx.setState(new EnJuegoState());
    }

    @Override
    public void finalizar(ScrimContext ctx) {
        throw new IllegalStateException("El scrim debe pasar primero a En juego antes de finalizar.");
    }

    @Override
    public void cancelar(ScrimContext ctx) {
        ctx.setState(new CanceladoState());
    }

    @Override
    public String getNombreEstado() {
        return "Confirmado";
    }
}
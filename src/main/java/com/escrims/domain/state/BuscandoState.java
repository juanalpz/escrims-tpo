package com.escrims.domain.state;

import com.escrims.domain.entities.RolJuego;
import com.escrims.domain.entities.Usuario;

/**
 * «ConcreteState» Estado inicial de un Scrim: buscando jugadores para
 * completar el lobby (RN-08).
 */
public class BuscandoState implements ScrimState {

    @Override
    public void postular(ScrimContext ctx, Usuario usuario, RolJuego rol) {
        // En este estado se admiten postulaciones. La lógica de aceptación o armado
        // de lobby se coordina con el contexto.
    }

    @Override
    public void confirmar(ScrimContext ctx, Usuario usuario) {
        throw new IllegalStateException("No se puede confirmar asistencia en estado 'Buscando'. El lobby aún no está completo.");
    }

    @Override
    public void iniciar(ScrimContext ctx) {
        throw new IllegalStateException("No se puede iniciar la partida mientras se buscan jugadores.");
    }

    @Override
    public void finalizar(ScrimContext ctx) {
        throw new IllegalStateException("No se puede finalizar una partida que aún está buscando jugadores.");
    }

    @Override
    public void cancelar(ScrimContext ctx) {
        // Cancelar permitido antes de EnJuegoState
    }

    @Override
    public String getNombreEstado() {
        return "Buscando jugadores";
    }
}

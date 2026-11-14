package com.escrims.domain.state;

import com.escrims.domain.entities.RolJuego;
import com.escrims.domain.entities.Usuario;

/**
 * Interfaz del Patrón State que define el contrato de acciones permitidas
 * en cada fase del ciclo de vida de un Scrim.
 */
public interface ScrimState {

    // Permite postular un usuario con un rol deseado (válido en BuscandoState)
    void postular(ScrimContext ctx, Usuario usuario, RolJuego rol);

    // Registra la confirmación de un jugador seleccionado (válido en
    // LobbyArmadoState)
    void confirmar(ScrimContext ctx, Usuario usuario);

    // Inicia la partida al llegar la fecha/hora (válido en ConfirmdoState vía
    // Scheduler)
    void iniciar(ScrimContext ctx);

    // Finaliza la partida y habilita carga de estadísticas (válido en EnJuegoState)
    void finalizar(ScrimContext ctx);

    // Cancela el encuentro (válido sólo antes de EnJuegoState)
    void cancelar(ScrimContext ctx);

    // Devuelve el nombre legible del estado (útil para eventos de dominio y logs)
    String getNombreEstado();
}

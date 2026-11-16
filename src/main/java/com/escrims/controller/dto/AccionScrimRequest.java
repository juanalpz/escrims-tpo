package com.escrims.controller.dto;

import com.escrims.domain.entities.LadoEquipo;
import com.escrims.domain.entities.RolJuego;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * DTO para solicitar acciones administrativas pre-partida sobre un scrim (Patrón Command).
 *
 * @param organizadorId       ID del organizador que ejecuta la acción.
 * @param jugadorPrincipalId  ID del jugador objetivo principal.
 * @param jugadorSecundarioId ID del segundo jugador involucrado (usado en intercambios).
 * @param nuevoRol            Nuevo rol a asignar al jugador.
 * @param ladoDestino         Equipo de destino (Equipo A o Equipo B).
 */
public record AccionScrimRequest(
        @NotNull(message = "El ID del organizador es obligatorio.") UUID organizadorId,
        UUID jugadorPrincipalId,
        UUID jugadorSecundarioId,
        RolJuego nuevoRol,
        LadoEquipo ladoDestino) {
}

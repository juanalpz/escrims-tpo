package com.escrims.controller.dto;

import com.escrims.domain.entities.LadoEquipo;
import com.escrims.domain.entities.RolJuego;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * DTO para POST /api/scrims/{id}/acciones/{command} (Patrón Command).
 */
public record AccionScrimRequest(
        @NotNull(message = "El ID del organizador es obligatorio.") UUID organizadorId,

        UUID jugadorPrincipalId,
        UUID jugadorSecundarioId,
        RolJuego nuevoRol,
        LadoEquipo ladoDestino) {
}

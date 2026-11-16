package com.escrims.controller.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * DTO de entrada para confirmar la participación de un jugador en un scrim armado (RN-09).
 *
 * @param usuarioId ID del usuario que confirma su asistencia al encuentro.
 */
public record ConfirmacionRequest(
        @NotNull(message = "El ID del usuario es obligatorio.") UUID usuarioId) {
}

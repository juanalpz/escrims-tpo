package com.escrims.controller.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * DTO de entrada para registrar el abandono o no-show de un jugador en un scrim.
 *
 * @param usuarioId ID del jugador que abandonó o no se presentó.
 * @param detalle   Observación opcional de quien registra la incidencia (máximo 300 caracteres).
 */
public record IncidenciaRequest(
        @NotNull(message = "El ID del usuario es obligatorio.") UUID usuarioId,

        @Size(max = 300, message = "El detalle no puede superar los 300 caracteres.") String detalle) {
}

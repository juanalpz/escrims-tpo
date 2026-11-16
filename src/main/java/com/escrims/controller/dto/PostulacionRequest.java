package com.escrims.controller.dto;

import com.escrims.domain.entities.RolJuego;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * DTO de entrada para registrar la postulación de un jugador a un scrim con un rol deseado.
 *
 * @param usuarioId  ID del usuario que solicita unirse al encuentro.
 * @param rolDeseado Rol dentro del juego que el participante desea ocupar.
 */
public record PostulacionRequest(
        @NotNull(message = "El ID del usuario es obligatorio.") UUID usuarioId,

        @NotNull(message = "El rol deseado es obligatorio.") RolJuego rolDeseado) {
}

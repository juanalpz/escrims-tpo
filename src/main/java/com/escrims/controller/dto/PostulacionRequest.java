package com.escrims.controller.dto;

import com.escrims.domain.entities.RolJuego;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PostulacionRequest(
        @NotNull(message = "El ID del usuario es obligatorio.") UUID usuarioId,

        @NotNull(message = "El rol deseado es obligatorio.") RolJuego rolDeseado) {
}

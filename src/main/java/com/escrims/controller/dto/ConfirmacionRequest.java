package com.escrims.controller.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ConfirmacionRequest(
        @NotNull(message = "El ID del usuario es obligatorio.") UUID usuarioId) {
}

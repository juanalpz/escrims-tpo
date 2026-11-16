package com.escrims.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record EstadisticaRequest(
        @NotNull(message = "El ID del usuario es obligatorio.") UUID usuarioId,

        boolean mvp,

        @NotBlank(message = "El KDA es obligatorio (ej. '18/6/4').") @Size(max = 30, message = "El formato KDA no puede superar los 30 caracteres.") String kda,

        @Size(max = 500, message = "Las observaciones no pueden superar los 500 caracteres.") String observaciones) {
}

package com.escrims.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record ReporteConductaRequest(
        @NotNull(message = "El ID del usuario reportado es obligatorio.") UUID reportadoId,

        @NotBlank(message = "El motivo del reporte es obligatorio.") @Size(min = 5, max = 300, message = "El motivo debe tener entre 5 y 300 caracteres.") String motivo) {
}

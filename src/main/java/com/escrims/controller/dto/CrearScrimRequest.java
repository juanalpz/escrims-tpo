package com.escrims.controller.dto;

import com.escrims.domain.entities.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record CrearScrimRequest(
        @NotNull(message = "El ID del organizador es obligatorio.") UUID organizadorId,

        @NotNull(message = "El juego es obligatorio.") Juego juego,

        @NotNull(message = "El formato es obligatorio.") Formato formato,

        @NotNull(message = "La región es obligatoria.") Region region,

        Rango rangoMin,
        Rango rangoMax,

        @NotNull(message = "La latencia máxima es obligatoria.") @Min(value = 1, message = "La latencia máxima debe ser al menos 1 ms (RN-04).") @Max(value = 500, message = "La latencia máxima no puede superar los 500 ms (RN-04).") Integer latenciaMax,

        @NotNull(message = "La fecha y hora del encuentro son obligatorias.") @Future(message = "La fecha y hora del scrim deben ser futuras.") LocalDateTime fechaHora,

        @Min(value = 15, message = "La duración mínima estimada es de 15 minutos.") @Max(value = 300, message = "La duración máxima estimada es de 300 minutos.") Integer duracionMinutos,

        Modalidad modalidad,

        Map<RolJuego, Integer> reglasRoles) {
}

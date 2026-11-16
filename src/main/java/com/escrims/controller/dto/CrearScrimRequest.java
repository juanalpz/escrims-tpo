package com.escrims.controller.dto;

import com.escrims.domain.entities.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * DTO de entrada para configurar y crear un nuevo scrim en el sistema.
 *
 * @param organizadorId   ID del usuario organizador que crea el encuentro.
 * @param juego           Videojuego en el que se disputará la partida.
 * @param formato         Formato de equipos del encuentro (ej. 5v5, 3v3, 1v1).
 * @param region          Región o servidor donde se jugará el scrim.
 * @param rangoMin        Rango mínimo admitido para postularse (opcional).
 * @param rangoMax        Rango máximo admitido para postularse (opcional).
 * @param latenciaMax     Latencia máxima permitida en milisegundos (entre 1 y 500 ms, RN-04).
 * @param fechaHora       Fecha y hora futura programada para el inicio del encuentro.
 * @param duracionMinutos Duración estimada de la partida en minutos (entre 15 y 300 min).
 * @param modalidad       Modalidad de juego del scrim (competitivo, casual, práctica).
 * @param reglasRoles     Mapa opcional con la cantidad de cupos requeridos por rol.
 */
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

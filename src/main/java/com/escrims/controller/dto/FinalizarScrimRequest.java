package com.escrims.controller.dto;

import jakarta.validation.constraints.Size;

/**
 * DTO de entrada opcional para finalizar un scrim informando su resultado.
 *
 * @param ganador  Equipo ganador (por ejemplo "EQUIPO_A").
 * @param marcador Marcador final en formato libre (por ejemplo "13-9").
 */
public record FinalizarScrimRequest(
        @Size(max = 60, message = "El ganador no puede superar los 60 caracteres.") String ganador,

        @Size(max = 30, message = "El marcador no puede superar los 30 caracteres.") String marcador) {
}

package com.escrims.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * DTO de entrada para registrar el rendimiento individual y observaciones al finalizar un scrim (RN-13).
 *
 * @param usuarioId     ID del jugador al que pertenecen las estadísticas.
 * @param mvp           Indica si el jugador fue elegido como el más valioso (MVP) de la partida.
 * @param kda           Registro de asesinatos, muertes y asistencias en formato texto (ej. "18/6/4").
 * @param observaciones Comentarios opcionales sobre el desempeño del jugador (máximo 500 caracteres).
 */
public record EstadisticaRequest(
        @NotNull(message = "El ID del usuario es obligatorio.") UUID usuarioId,

        boolean mvp,

        @NotBlank(message = "El KDA es obligatorio (ej. '18/6/4').") @Size(max = 30, message = "El formato KDA no puede superar los 30 caracteres.") String kda,

        @Size(max = 500, message = "Las observaciones no pueden superar los 500 caracteres.") String observaciones) {
}

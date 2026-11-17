package com.escrims.controller.dto;

import jakarta.validation.constraints.Size;

/**
 * DTO de entrada opcional para cancelar un scrim indicando el motivo (RN-12).
 *
 * @param motivo Motivo de la cancelación (máximo 300 caracteres).
 */
public record CancelarScrimRequest(
        @Size(max = 300, message = "El motivo no puede superar los 300 caracteres.") String motivo) {
}

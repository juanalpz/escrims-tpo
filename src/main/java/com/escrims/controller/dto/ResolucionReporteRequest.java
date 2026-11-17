package com.escrims.controller.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada con la decisión de un moderador humano sobre un reporte en revisión (CU11).
 *
 * @param sancionar true para aplicar un strike al reportado; false para desestimar el reporte.
 * @param nota      Fundamento de la decisión (máximo 300 caracteres).
 */
public record ResolucionReporteRequest(
        @NotNull(message = "Debe indicar si corresponde sancionar.") Boolean sancionar,

        @Size(max = 300, message = "La nota no puede superar los 300 caracteres.") String nota) {
}

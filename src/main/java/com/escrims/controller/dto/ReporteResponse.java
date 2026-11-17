package com.escrims.controller.dto;

import com.escrims.domain.entities.EstadoReporte;
import com.escrims.domain.entities.ReporteConducta;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de salida de un reporte de conducta y de su paso por la cadena de moderación (CU11).
 *
 * @param id            Identificador del reporte.
 * @param scrimId       Scrim en el que ocurrió la conducta.
 * @param reportanteId  Usuario que denunció.
 * @param reportadoId   Usuario denunciado.
 * @param motivo        Descripción de la conducta.
 * @param estado        Estado del reporte (RECHAZADO, RESUELTO o EN_REVISION).
 * @param resolucion    Resolución de la última intervención, o null al listar reportes.
 * @param sancion       Sanción aplicada, o null si no hubo.
 * @param fechaCreacion Momento del reporte.
 */
public record ReporteResponse(
        UUID id,
        UUID scrimId,
        UUID reportanteId,
        UUID reportadoId,
        String motivo,
        EstadoReporte estado,
        String resolucion,
        SancionResponse sancion,
        LocalDateTime fechaCreacion) {

    public static ReporteResponse fromDomain(ReporteConducta r, String resolucion) {
        return new ReporteResponse(r.getId(), r.getScrim().getId(), r.getReportante().getId(),
                r.getReportado().getId(), r.getMotivo(), r.getEstado(), resolucion,
                SancionResponse.fromDomain(r.getSancion()), r.getFechaCreacion());
    }
}

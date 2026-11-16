package com.escrims.domain.moderation;

import com.escrims.domain.entities.ReporteConducta;
import com.escrims.domain.entities.ResultadoModeracion;

/**
 * Implementa un ConcreteHandler del patrón Chain of Responsibility
 * que resuelve automáticamente reportes de usuarios sin strikes previos.
 */
public class AutoResolverHandler extends ReportHandler {

    private static final int UMBRAL_STRIKES_AUTO = 0;

    /**
     * Evalúa si un reporte puede resolverse automáticamente según
     * el historial de strikes del usuario reportado.
     *
     * @param reporte reporte de conducta que se desea evaluar
     * @return resultado sin sanción si el usuario no tiene strikes previos;
     *         resultado sin resolver en caso contrario
     * @throws IllegalStateException si el reporte ya fue resuelto o rechazado
     *         y se intenta desestimarlo automáticamente
     */
    @Override
    protected ResultadoModeracion intentarResolver(ReporteConducta reporte) {
        // Regla de moderación: los reportes de usuarios sin strikes previos se desestiman.
        if (reporte.getReportado().getStrikes() <= UMBRAL_STRIKES_AUTO) {
            reporte.rechazar();
            return ResultadoModeracion.resueltoSinSancion(
                    "Auto-resuelto: el reportado no tiene historial negativo.");
        }
        return ResultadoModeracion.sinResolver();
    }
}

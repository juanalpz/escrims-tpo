package com.escrims.domain.moderation;

import com.escrims.domain.entities.ReporteConducta;
import com.escrims.domain.entities.ResultadoModeracion;

/**
 * Implementa un ConcreteHandler del patrón Chain of Responsibility
 * que escala los reportes pendientes a revisión por un moderador humano.
 */
public class HumanModeratorHandler extends ReportHandler {

    /**
     * Escala un reporte pendiente a revisión humana cuando no pudo
     * ser resuelto por los manejadores anteriores.
     *
     * @param reporte reporte de conducta que se desea escalar
     * @return resultado que indica el escalamiento a revisión humana,
     *         sin sanción asociada
     * @throws IllegalStateException si el reporte no está en estado pendiente
     */
    @Override
    protected ResultadoModeracion intentarResolver(ReporteConducta reporte) {
        reporte.escalarARevision();
        return ResultadoModeracion.resueltoSinSancion(
                "Escalado a moderador humano para revisión manual.");
    }
}

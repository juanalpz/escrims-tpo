package com.escrims.domain.moderation;

import com.escrims.domain.entities.ReporteConducta;
import com.escrims.domain.entities.ResultadoModeracion;

/**
 * Define el Handler abstracto del patrón Chain of Responsibility
 * para procesar reportes de conducta mediante una cadena de moderadores.
 */
public abstract class ReportHandler {

    private ReportHandler siguiente;

    /**
     * Establece el siguiente manejador de la cadena de moderación.
     *
     * @param siguiente manejador que procesará los reportes no resueltos,
     *                  o null si es el último eslabón
     */
    public void setSiguiente(ReportHandler siguiente) {
        this.siguiente = siguiente;
    }

    /**
     * Procesa un reporte y delega su resolución al siguiente manejador
     * si el actual no puede resolverlo.
     *
     * @param reporte reporte de conducta que se desea procesar
     * @return resultado de la moderación o resultado sin resolver
     *         si ningún manejador logra procesarlo
     */
    public ResultadoModeracion manejar(ReporteConducta reporte) {
        ResultadoModeracion resultado = intentarResolver(reporte);
        if (resultado.isResuelto()) {
            return resultado;
        }
        if (siguiente != null) {
            return siguiente.manejar(reporte);
        }
        return ResultadoModeracion.sinResolver();
    }

    /**
     * Define la operación de resolución que debe implementar cada
     * manejador concreto de la cadena de moderación.
     *
     * @param reporte reporte de conducta que se desea evaluar
     * @return resultado de la evaluación del manejador
     */
    protected abstract ResultadoModeracion intentarResolver(ReporteConducta reporte);
}

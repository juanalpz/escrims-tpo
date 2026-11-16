package com.escrims.domain.entities;

/**
 * Representa el resultado de la evaluación de un reporte de conducta
 * dentro del proceso de moderación, con o sin sanción asociada.
 */
public class ResultadoModeracion {

    private final boolean resuelto;
    private final String resolucion;
    private final Sancion sancion;

    /**
     * Crea un resultado de moderación con su estado, resolución y sanción.
     *
     * @param resuelto indica si el reporte fue resuelto
     * @param resolucion descripción de la resolución adoptada
     * @param sancion sanción aplicada, si corresponde
     */
    public ResultadoModeracion(boolean resuelto, String resolucion, Sancion sancion) {
        this.resuelto = resuelto;
        this.resolucion = resolucion;
        this.sancion = sancion;
    }

    /**
     * Crea un resultado que representa un reporte aún sin resolver.
     *
     * @return resultado de moderación pendiente de resolución
     */
    public static ResultadoModeracion sinResolver() {
        return new ResultadoModeracion(false, null, null);
    }

    /**
     * Crea un resultado de moderación resuelto sin aplicar sanciones.
     *
     * @param resolucion descripción de la resolución adoptada
     * @return resultado de moderación sin sanción
     */
    public static ResultadoModeracion resueltoSinSancion(String resolucion) {
        return new ResultadoModeracion(true, resolucion, null);
    }

    /**
     * Crea un resultado de moderación resuelto con una sanción asociada.
     *
     * @param resolucion descripción de la resolución adoptada
     * @param sancion sanción asociada a la resolución
     * @return resultado de moderación con sanción
     */
    public static ResultadoModeracion resueltoConSancion(String resolucion, Sancion sancion) {
        return new ResultadoModeracion(true, resolucion, sancion);
    }

    /**
     * Indica si el reporte fue resuelto.
     *
     * @return true si el reporte está resuelto; false en caso contrario
     */
    public boolean isResuelto() { return resuelto; }

    /**
     * Obtiene la descripción de la resolución adoptada.
     *
     * @return descripción de la resolución o null si no fue definida
     */
    public String getResolucion() { return resolucion; }

    /**
     * Obtiene la sanción asociada al resultado de moderación.
     *
     * @return sanción asociada o null si no se aplicó ninguna
     */
    public Sancion getSancion() { return sancion; }
}

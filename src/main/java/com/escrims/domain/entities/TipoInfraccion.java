package com.escrims.domain.entities;

/**
 * Clasifica las infracciones que generan strikes en el sistema de penalidades.
 */
public enum TipoInfraccion {
    /** El jugador dejó una partida ya iniciada. */
    ABANDONO,
    /** El jugador no confirmó o no se presentó a un scrim con lobby armado. */
    NO_SHOW,
    /** Sanción derivada de un reporte de conducta (CU11). */
    CONDUCTA
}

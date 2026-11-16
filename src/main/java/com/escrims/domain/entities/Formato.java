package com.escrims.domain.entities;

/**
 * Define el formato de un scrim según la cantidad de jugadores por equipo.
 */
public enum Formato {

    UNO_VS_UNO(1),
    TRES_VS_TRES(3),
    CINCO_VS_CINCO(5);

    private final int jugadoresPorLado;

    /**
     * Inicializa el formato con la cantidad de jugadores por equipo.
     *
     * @param jugadoresPorLado cantidad de jugadores por equipo
     */
    Formato(int jugadoresPorLado) {
        this.jugadoresPorLado = jugadoresPorLado;
    }

    /**
     * Obtiene la cantidad de jugadores por equipo.
     *
     * @return cantidad de jugadores por lado
     */
    public int getJugadoresPorLado() {
        return jugadoresPorLado;
    }

    /**
     * Calcula los cupos totales del scrim considerando ambos equipos.
     *
     * @return cantidad total de cupos disponibles
     */
    public int getCuposTotales() {
        return jugadoresPorLado * 2;
    }
}

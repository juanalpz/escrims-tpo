package com.escrims.domain.entities;

/** Formato del scrim según la cantidad de jugadores por lado. */
public enum Formato {

    UNO_VS_UNO(1),
    TRES_VS_TRES(3),
    CINCO_VS_CINCO(5);

    private final int jugadoresPorLado;

    Formato(int jugadoresPorLado) {
        this.jugadoresPorLado = jugadoresPorLado;
    }

    public int getJugadoresPorLado() {
        return jugadoresPorLado;
    }

    /** Cupos totales = jugadores por lado × 2. */
    public int getCuposTotales() {
        return jugadoresPorLado * 2;
    }
}

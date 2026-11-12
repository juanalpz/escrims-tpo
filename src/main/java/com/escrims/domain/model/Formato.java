package com.escrims.domain.model;

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

    public int getCuposTotales() {
        return jugadoresPorLado * 2;
    }
}

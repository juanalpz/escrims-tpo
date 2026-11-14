package com.escrims.domain.entities;

/**
 * Rangos competitivos de eSports, cada uno con un intervalo de MMR asociado.
 * El ordinal define el orden natural (Hierro < Bronce < ... < Radiante),
 * lo que permite comparar rangos con {@code ordinal()}.
 */
public enum Rango {

    HIERRO(0, 499),
    BRONCE(500, 999),
    PLATA(1000, 1199),
    ORO(1200, 1499),
    PLATINO(1500, 1800),
    DIAMANTE(1801, 2499),
    RADIANTE(2500, 3000);

    private final int mmrMin;
    private final int mmrMax;

    Rango(int mmrMin, int mmrMax) {
        this.mmrMin = mmrMin;
        this.mmrMax = mmrMax;
    }

    public int getMmrMin() {
        return mmrMin;
    }

    public int getMmrMax() {
        return mmrMax;
    }

    /** Indica si un valor de MMR cae dentro de este rango. */
    public boolean contiene(int mmr) {
        return mmr >= mmrMin && mmr <= mmrMax;
    }

    /** Devuelve el {@code Rango} al que corresponde un MMR dado. */
    public static Rango desdeMMR(int mmr) {
        for (Rango r : values()) {
            if (r.contiene(mmr)) {
                return r;
            }
        }
        throw new IllegalArgumentException("MMR fuera de cualquier rango: " + mmr);
    }
}

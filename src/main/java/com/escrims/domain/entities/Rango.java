package com.escrims.domain.entities;

/**
 * Define los rangos competitivos de eSports y sus intervalos de MMR,
 * ordenados jerárquicamente desde Hierro hasta Radiante.
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

    /**
     * Inicializa el rango competitivo con sus límites de MMR.
     *
     * @param mmrMin valor mínimo de MMR del rango
     * @param mmrMax valor máximo de MMR del rango
     */
    Rango(int mmrMin, int mmrMax) {
        this.mmrMin = mmrMin;
        this.mmrMax = mmrMax;
    }

    /**
     * Obtiene el límite inferior de MMR del rango.
     *
     * @return valor mínimo de MMR
     */
    public int getMmrMin() {
        return mmrMin;
    }

    /**
     * Obtiene el límite superior de MMR del rango.
     *
     * @return valor máximo de MMR
     */
    public int getMmrMax() {
        return mmrMax;
    }

    /**
     * Verifica si un valor de MMR pertenece al intervalo del rango,
     * incluyendo sus límites.
     *
     * @param mmr puntuación competitiva que se desea evaluar
     * @return true si el MMR pertenece al rango; false en caso contrario
     */
    public boolean contiene(int mmr) {
        return mmr >= mmrMin && mmr <= mmrMax;
    }

    /**
     * Determina el rango competitivo correspondiente a un valor de MMR.
     *
     * @param mmr puntuación competitiva que se desea clasificar
     * @return rango competitivo asociado al MMR
     * @throws IllegalArgumentException si el MMR no pertenece a ningún rango definido
     */
    public static Rango desdeMMR(int mmr) {
        for (Rango r : values()) {
            if (r.contiene(mmr)) {
                return r;
            }
        }
        throw new IllegalArgumentException("MMR fuera de cualquier rango: " + mmr);
    }
}

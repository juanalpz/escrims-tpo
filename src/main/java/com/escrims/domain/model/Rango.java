package com.escrims.domain.model;

/** Límites de MMR (mínimo y máximo, ambos inclusive) que admite un scrim. */
public record Rango(int minimo, int maximo) {

    public Rango {
        if (minimo > maximo) {
            throw new IllegalArgumentException("El rango mínimo no puede superar al máximo");
        }
    }

    public boolean contiene(int mmr) {
        return mmr >= minimo && mmr <= maximo;
    }
}

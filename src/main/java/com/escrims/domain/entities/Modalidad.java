package com.escrims.domain.entities;

/**
 * Define el tipo de modalidad competitiva o práctica del Scrim.
 */
public enum Modalidad {
    RANKED_LIKE("Competitivo / Ranked-like"),
    CASUAL("Casual / Amistoso"),
    PRACTICA_ESTRATOS("Práctica de Estrategias");

    private final String descripcion;

    Modalidad(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
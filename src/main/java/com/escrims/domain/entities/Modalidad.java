package com.escrims.domain.entities;

/**
 * Define las modalidades competitivas y de práctica disponibles para un scrim.
 */
public enum Modalidad {

    RANKED_LIKE("Competitivo / Ranked-like"),
    CASUAL("Casual / Amistoso"),
    PRACTICA_ESTRATOS("Práctica de Estrategias");

    private final String descripcion;

    /**
     * Inicializa la modalidad con su descripción.
     *
     * @param descripcion descripción de la modalidad
     */
    Modalidad(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * Obtiene la descripción de la modalidad.
     *
     * @return descripción de la modalidad
     */
    public String getDescripcion() {
        return descripcion;
    }
}

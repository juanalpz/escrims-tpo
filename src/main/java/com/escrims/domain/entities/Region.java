package com.escrims.domain.entities;

/**
 * Define las regiones disponibles para disputar scrims
 * y sus respectivos servidores de juego.
 */
public enum Region {

    LAS("LAS", "Buenos Aires"),
    SA("SA", "São Paulo"),
    NA("NA", "Virginia"),
    EU("EU", "Frankfurt");

    private final String codigo;
    private final String servidor;

    /**
     * Inicializa la región con su código y servidor asociado.
     *
     * @param codigo identificador de la región
     * @param servidor ubicación del servidor de juego
     */
    Region(String codigo, String servidor) {
        this.codigo = codigo;
        this.servidor = servidor;
    }

    /**
     * Obtiene el código identificador de la región.
     *
     * @return código de la región
     */
    public String getCodigo() {
        return codigo;
    }

    /**
     * Obtiene la ubicación del servidor asociado a la región.
     *
     * @return ubicación del servidor de juego
     */
    public String getServidor() {
        return servidor;
    }
}

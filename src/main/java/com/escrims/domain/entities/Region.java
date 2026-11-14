package com.escrims.domain.entities;

/** Región/servidor donde se juega. */
public enum Region {

    LAS("LAS", "Buenos Aires"),
    SA("SA", "São Paulo"),
    NA("NA", "Virginia"),
    EU("EU", "Frankfurt");

    private final String codigo;
    private final String servidor;

    Region(String codigo, String servidor) {
        this.codigo = codigo;
        this.servidor = servidor;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getServidor() {
        return servidor;
    }
}

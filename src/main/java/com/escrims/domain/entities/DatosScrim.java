package com.escrims.domain.entities;

import java.time.LocalDateTime;

/**
 * Value Object que agrupa los criterios y parámetros básicos de configuración o filtrado de un scrim.
 */
public class DatosScrim {

    private final Juego juego;
    private final Formato formato;
    private final Rango rangoMin;
    private final Rango rangoMax;
    private final Region region;
    private final LocalDateTime inicio;
    private final int latenciaMaxima;

    /**
     * Inicializa el objeto de datos con los parámetros de búsqueda o creación del encuentro.
     *
     * @param juego          videojuego del encuentro.
     * @param formato        formato de equipos (ej. 5v5, 3v3, 1v1).
     * @param rangoMin       rango mínimo admitido.
     * @param rangoMax       rango máximo admitido.
     * @param region         región o servidor del encuentro.
     * @param inicio         fecha y hora programada de inicio.
     * @param latenciaMaxima latencia máxima permitida en milisegundos.
     */
    public DatosScrim(Juego juego, Formato formato, Rango rangoMin, Rango rangoMax,
                      Region region, LocalDateTime inicio, int latenciaMaxima) {
        this.juego = juego;
        this.formato = formato;
        this.rangoMin = rangoMin;
        this.rangoMax = rangoMax;
        this.region = region;
        this.inicio = inicio;
        this.latenciaMaxima = latenciaMaxima;
    }

    /**
     * Verifica que los campos esenciales del encuentro (juego, formato, región e inicio) estén presentes.
     *
     * @return {@code true} si los parámetros mínimos son válidos, o {@code false} en caso contrario.
     */
    public boolean esValido() {
        return juego != null && formato != null && region != null && inicio != null;
    }

    public Juego getJuego() { return juego; }
    public Formato getFormato() { return formato; }
    public Rango getRangoMin() { return rangoMin; }
    public Rango getRangoMax() { return rangoMax; }
    public Region getRegion() { return region; }
    public LocalDateTime getInicio() { return inicio; }
    public int getLatenciaMaxima() { return latenciaMaxima; }
}

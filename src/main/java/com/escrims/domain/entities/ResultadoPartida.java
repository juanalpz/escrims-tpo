package com.escrims.domain.entities;

/**
 * Representa el resultado final de un scrim, incluyendo
 * el equipo ganador y el marcador de la partida.
 */
public class ResultadoPartida {

    private final String ganador;
    private final String marcador;

    /**
     * Crea un resultado de partida con su ganador y marcador.
     *
     * @param ganador identificador del ganador de la partida
     * @param marcador resultado numérico de la partida
     */
    public ResultadoPartida(String ganador, String marcador) {
        this.ganador = ganador;
        this.marcador = marcador;
    }

    /**
     * Obtiene el ganador de la partida.
     *
     * @return identificador del ganador
     */
    public String getGanador() { return ganador; }

    /**
     * Obtiene el marcador final de la partida.
     *
     * @return marcador de la partida
     */
    public String getMarcador() { return marcador; }

    /**
     * Genera una representación textual del resultado de la partida.
     *
     * @return cadena con el ganador y el marcador final
     */
    @Override
    public String toString() {
        return "Ganador: " + ganador + " (" + marcador + ")";
    }
}

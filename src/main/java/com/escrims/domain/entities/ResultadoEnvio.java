package com.escrims.domain.entities;

/**
 * Value Object que representa el resultado de un envío de notificación,
 * indicando su éxito y la cantidad de intentos realizados.
 */
public class ResultadoEnvio {

    private final boolean exitoso;
    private final int intentos;

    /**
     * Crea un resultado de envío con su estado y cantidad de intentos.
     *
     * @param exitoso indica si el envío fue exitoso
     * @param intentos cantidad de intentos realizados
     */
    public ResultadoEnvio(boolean exitoso, int intentos) {
        this.exitoso = exitoso;
        this.intentos = intentos;
    }

    /**
     * Indica si el envío de la notificación fue exitoso.
     *
     * @return true si el envío fue exitoso; false en caso contrario
     */
    public boolean isExitoso() { return exitoso; }

    /**
     * Obtiene la cantidad de intentos de envío realizados.
     *
     * @return número de intentos realizados
     */
    public int getIntentos() { return intentos; }

    /**
     * Crea un resultado que representa un envío exitoso.
     *
     * @param intentos cantidad de intentos realizados
     * @return resultado de envío exitoso
     */
    public static ResultadoEnvio exito(int intentos) {
        return new ResultadoEnvio(true, intentos);
    }

    /**
     * Crea un resultado que representa un envío fallido.
     *
     * @param intentos cantidad de intentos realizados
     * @return resultado de envío fallido
     */
    public static ResultadoEnvio fallo(int intentos) {
        return new ResultadoEnvio(false, intentos);
    }
}

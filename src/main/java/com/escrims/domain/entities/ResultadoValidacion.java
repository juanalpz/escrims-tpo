package com.escrims.domain.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa el resultado de la validación de configuración de un scrim,
 * indicando su validez y los errores detectados.
 */
public class ResultadoValidacion {

    private final boolean valido;
    private final List<String> errores;

    /**
     * Inicializa el resultado de validación con su estado y lista de errores.
     *
     * @param valido indica si la configuración es válida
     * @param errores lista de errores detectados durante la validación
     */
    private ResultadoValidacion(boolean valido, List<String> errores) {
        this.valido = valido;
        this.errores = errores;
    }

    /**
     * Crea un resultado de validación exitoso sin errores.
     *
     * @return resultado de validación válido
     */
    public static ResultadoValidacion ok() {
        return new ResultadoValidacion(true, Collections.emptyList());
    }

    /**
     * Crea un resultado de validación con los errores detectados.
     *
     * @param errores lista de errores de configuración
     * @return resultado de validación inválido con una copia de los errores
     * @throws NullPointerException si la lista de errores es nula
     */
    public static ResultadoValidacion conErrores(List<String> errores) {
        return new ResultadoValidacion(false, new ArrayList<>(errores));
    }

    /**
     * Indica si la configuración del scrim es válida.
     *
     * @return true si la configuración es válida; false en caso contrario
     */
    public boolean isValido() { return valido; }

    /**
     * Obtiene los errores detectados durante la validación.
     *
     * @return lista no modificable de errores de configuración
     */
    public List<String> getErrores() { return Collections.unmodifiableList(errores); }
}

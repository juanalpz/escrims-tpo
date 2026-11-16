package com.escrims.domain.validation;

import com.escrims.domain.entities.DatosScrim;
import com.escrims.domain.entities.ResultadoValidacion;

import java.util.ArrayList;
import java.util.List;

/**
 * Define un validador abstracto mediante el patrón Template Method,
 * delegando las reglas específicas de cada juego a sus subclases.
 */
public abstract class ConfiguracionScrimValidator {

    /**
     * Ejecuta el algoritmo de validación de configuración del scrim,
     * verificando roles, cantidad de jugadores y reglas específicas del juego.
     *
     * @param datos configuración del scrim que se desea validar
     * @return resultado de validación con los errores detectados,
     *         o un resultado válido si no existen errores
     */
    public final ResultadoValidacion validar(DatosScrim datos) {
        List<String> errores = new ArrayList<>();
        validarRoles(datos, errores);
        validarCantidad(datos, errores);
        validarReglasJuego(datos, errores);
        return errores.isEmpty() ? ResultadoValidacion.ok() : ResultadoValidacion.conErrores(errores);
    }

    /**
     * Define la validación de la composición de roles según el juego.
     *
     * @param datos configuración del scrim que se desea evaluar
     * @param errores lista donde se registran los errores detectados
     */
    protected abstract void validarRoles(DatosScrim datos, List<String> errores);

    /**
     * Define la validación de la cantidad de jugadores por equipo.
     *
     * @param datos configuración del scrim que se desea evaluar
     * @param errores lista donde se registran los errores detectados
     */
    protected abstract void validarCantidad(DatosScrim datos, List<String> errores);

    /**
     * Define la validación de las reglas particulares de cada juego.
     *
     * @param datos configuración del scrim que se desea evaluar
     * @param errores lista donde se registran los errores detectados
     */
    protected abstract void validarReglasJuego(DatosScrim datos, List<String> errores);
}

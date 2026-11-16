package com.escrims.domain.validation;

import com.escrims.domain.entities.DatosScrim;

import java.util.List;

/**
 * Implementa una ConcreteClass del patrón Template Method para validar
 * la configuración de scrims correspondientes a League of Legends.
 */
public class LoLConfigValidator extends ConfiguracionScrimValidator {

    /**
     * Define la validación de los roles permitidos en League of Legends.
     *
     * @param datos configuración del scrim que se desea validar
     * @param errores lista donde se registran los errores detectados
     */
    @Override
    protected void validarRoles(DatosScrim datos, List<String> errores) {
        // LoL: Roles típicos TOP, JG, MID, ADC, SUPPORT
    }

    /**
     * Define la validación de la cantidad de jugadores por equipo
     * en League of Legends.
     *
     * @param datos configuración del scrim que se desea validar
     * @param errores lista donde se registran los errores detectados
     */
    @Override
    protected void validarCantidad(DatosScrim datos, List<String> errores) {
        // LoL: 5v5 es el formato estándar
    }

    /**
     * Define la validación de las reglas específicas de League of Legends.
     *
     * @param datos configuración del scrim que se desea validar
     * @param errores lista donde se registran los errores detectados
     */
    @Override
    protected void validarReglasJuego(DatosScrim datos, List<String> errores) {
        // Reglas específicas de LoL
    }
}

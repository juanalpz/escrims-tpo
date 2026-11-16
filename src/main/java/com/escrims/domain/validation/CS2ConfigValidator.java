package com.escrims.domain.validation;

import com.escrims.domain.entities.DatosScrim;

import java.util.List;

/**
 * Implementa una ConcreteClass del patrón Template Method para validar
 * la configuración de scrims correspondientes a CS2.
 */
public class CS2ConfigValidator extends ConfiguracionScrimValidator {

    /**
     * Define la validación de los roles permitidos en CS2.
     *
     * @param datos configuración del scrim que se desea validar
     * @param errores lista donde se registran los errores detectados
     */
    @Override
    protected void validarRoles(DatosScrim datos, List<String> errores) {
        // CS2: Roles ENTRY, AWP, LURKER, IGL, RIFLER
    }

    /**
     * Define la validación de la cantidad de jugadores por equipo en CS2.
     *
     * @param datos configuración del scrim que se desea validar
     * @param errores lista donde se registran los errores detectados
     */
    @Override
    protected void validarCantidad(DatosScrim datos, List<String> errores) {
        // CS2: 5v5 estándar, 1v1 para duelos
    }

    /**
     * Define la validación de las reglas específicas de CS2.
     *
     * @param datos configuración del scrim que se desea validar
     * @param errores lista donde se registran los errores detectados
     */
    @Override
    protected void validarReglasJuego(DatosScrim datos, List<String> errores) {
        // Reglas específicas de CS2
    }
}

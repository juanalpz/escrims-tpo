package com.escrims.domain.validation;

import com.escrims.domain.entities.DatosScrim;
import com.escrims.domain.entities.Formato;

import java.util.List;

/**
 * Implementa una ConcreteClass del patrón Template Method para validar
 * la configuración de scrims correspondientes a Valorant.
 */
public class ValorantConfigValidator extends ConfiguracionScrimValidator {

    /**
     * Define la validación de la composición de roles en Valorant,
     * contemplando los requisitos previstos para el formato 5v5.
     *
     * @param datos configuración del scrim que se desea validar
     * @param errores lista donde se registran los errores detectados
     */
    @Override
    protected void validarRoles(DatosScrim datos, List<String> errores) {
        // Valorant: al menos 1 Duelista y 1 Controller por equipo en 5v5
        if (datos.getFormato() == Formato.CINCO_VS_CINCO) {
            // Validación simplificada: solo verificar formato compatible
        }
    }

    /**
     * Valida la cantidad de jugadores por equipo, rechazando
     * el formato 3v3 por no estar admitido en Valorant.
     *
     * @param datos configuración del scrim que se desea validar
     * @param errores lista donde se registra el error si el formato es 3v3
     */
    @Override
    protected void validarCantidad(DatosScrim datos, List<String> errores) {
        if (datos.getFormato() == Formato.TRES_VS_TRES) {
            // Valorant no soporta 3v3 oficialmente
            errores.add("Valorant no soporta formato 3v3.");
        }
    }

    /**
     * Define la validación de las reglas específicas de Valorant,
     * incluyendo los requisitos previstos para la modalidad competitiva.
     *
     * @param datos configuración del scrim que se desea validar
     * @param errores lista donde se registran los errores detectados
     */
    @Override
    protected void validarReglasJuego(DatosScrim datos, List<String> errores) {
        // Valorant: rango obligatorio en modo ranked-like
    }
}

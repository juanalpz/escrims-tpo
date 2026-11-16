package com.escrims.domain.moderation;

import com.escrims.domain.entities.Sancion;
import com.escrims.domain.entities.TipoInfraccion;
import com.escrims.domain.entities.Usuario;

import java.time.LocalDateTime;

/**
 * Política del sistema de strikes: cada infracción suma un strike y el
 * cooldown crece con la reincidencia (RN-06).
 *
 * <ul>
 *   <li>1.er strike: advertencia, sin cooldown.</li>
 *   <li>2.º strike: 24 horas sin poder postularse ni organizar.</li>
 *   <li>3.er strike: 72 horas.</li>
 *   <li>4.º strike en adelante: 7 días.</li>
 * </ul>
 */
public class PoliticaSanciones {

    /** Strikes que suma cada infracción. */
    public static final int STRIKES_POR_INFRACCION = 1;

    /** A partir de esta cantidad de strikes los reportes se derivan a un moderador humano. */
    public static final int STRIKES_REVISION_HUMANA = 3;

    private static final int[] HORAS_COOLDOWN_POR_STRIKE = {0, 24, 72};
    private static final int HORAS_COOLDOWN_MAXIMO = 24 * 7;

    /**
     * Calcula las horas de cooldown que corresponden al alcanzar un total de strikes.
     *
     * @param strikesAcumulados total de strikes del usuario contando la infracción actual
     * @return horas de cooldown; cero si se trata de la primera infracción
     * @throws IllegalArgumentException si el total es menor a uno
     */
    public int horasDeCooldown(int strikesAcumulados) {
        if (strikesAcumulados < 1) {
            throw new IllegalArgumentException("El total de strikes debe ser al menos 1.");
        }
        return strikesAcumulados <= HORAS_COOLDOWN_POR_STRIKE.length
                ? HORAS_COOLDOWN_POR_STRIKE[strikesAcumulados - 1]
                : HORAS_COOLDOWN_MAXIMO;
    }

    /**
     * Construye la sanción que corresponde a una nueva infracción, sin
     * registrarla todavía en el historial del usuario.
     *
     * @param usuario jugador infractor
     * @param tipo infracción cometida
     * @param motivo descripción de la causa
     * @param ahora momento desde el cual se cuenta el cooldown
     * @return sanción con un strike y el cooldown escalonado correspondiente
     * @throws IllegalArgumentException si el usuario, el tipo o el momento son nulos
     */
    public Sancion crearSancion(Usuario usuario, TipoInfraccion tipo, String motivo, LocalDateTime ahora) {
        if (usuario == null || tipo == null || ahora == null) {
            throw new IllegalArgumentException("El usuario, el tipo de infracción y el momento son obligatorios.");
        }
        int horas = horasDeCooldown(usuario.getStrikes() + STRIKES_POR_INFRACCION);
        LocalDateTime cooldownHasta = horas > 0 ? ahora.plusHours(horas) : null;
        return new Sancion(STRIKES_POR_INFRACCION, cooldownHasta, tipo, motivo);
    }

    /**
     * Construye la sanción de una nueva infracción y la agrega al historial del usuario.
     *
     * @param usuario jugador infractor
     * @param tipo infracción cometida
     * @param motivo descripción de la causa
     * @param ahora momento desde el cual se cuenta el cooldown
     * @return sanción ya registrada en el usuario
     */
    public Sancion aplicar(Usuario usuario, TipoInfraccion tipo, String motivo, LocalDateTime ahora) {
        Sancion sancion = crearSancion(usuario, tipo, motivo, ahora);
        usuario.agregarSancion(sancion);
        return sancion;
    }
}

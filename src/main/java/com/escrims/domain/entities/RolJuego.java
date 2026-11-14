package com.escrims.domain.entities;

/**
 * Roles que un jugador puede desempeñar dentro de un equipo.
 * Incluye roles específicos de cada juego soportado y alias de compatibilidad.
 */
public enum RolJuego {
    // Valorant
    DUELISTA,
    DUELIST,
    CENTINELA,
    INICIADOR,
    CONTROLADOR,
    CONTROLLER,

    // LoL
    TOP,
    JUNGLA,
    MID,
    ADC,
    SUPPORT,

    // CS2
    ENTRY,
    AWP,
    LURKER,
    IGL,
    RIFLER,

    // Genérico (compatibilidad)
    TANQUE
}

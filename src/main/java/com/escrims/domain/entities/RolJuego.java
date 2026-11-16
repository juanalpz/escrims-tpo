package com.escrims.domain.entities;

/**
 * Define los roles que un jugador puede desempeñar dentro de un equipo,
 * incluyendo roles específicos de cada juego y alias de compatibilidad.
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

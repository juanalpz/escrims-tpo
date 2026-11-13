package com.escrims.domain.model;

import java.time.LocalDateTime;

/** Strikes acumulados por un usuario y cooldown durante el cual no puede jugar. */
public class Sancion {

    private final int strikes;
    private final LocalDateTime cooldownHasta;

    public Sancion(int strikes, LocalDateTime cooldownHasta) {
        this.strikes = strikes;
        this.cooldownHasta = cooldownHasta;
    }

    public int getStrikes() {
        return strikes;
    }

    public LocalDateTime getCooldownHasta() {
        return cooldownHasta;
    }

    public boolean estaActiva() {
        return estaActiva(LocalDateTime.now());
    }

    public boolean estaActiva(LocalDateTime momento) {
        return cooldownHasta != null && momento.isBefore(cooldownHasta);
    }
}

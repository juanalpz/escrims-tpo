package com.escrims.domain.entities;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Strikes acumulados por un usuario y cooldown durante el cual no puede jugar.
 * Entidad con identidad propia (UUID), equals/hashCode por id.
 */
public class Sancion {

    private final UUID id;
    private final int strikes;
    private final LocalDateTime cooldownHasta;

    public Sancion(int strikes, LocalDateTime cooldownHasta) {
        if (strikes <= 0) {
            throw new IllegalArgumentException("Los strikes deben ser mayores a 0.");
        }
        this.id = UUID.randomUUID();
        this.strikes = strikes;
        this.cooldownHasta = cooldownHasta;
    }

    public UUID getId() {
        return id;
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

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Sancion s && id.equals(s.id));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

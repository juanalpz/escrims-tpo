package com.escrims.domain.entities;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Representa una sanción aplicada a un usuario, definida por una cantidad
 * de strikes y un período de restricción de participación (cooldown).
 */
public class Sancion {

    private final UUID id;
    private final int strikes;
    private final LocalDateTime cooldownHasta;

    /**
     * Crea una sanción con la cantidad de strikes y el límite del cooldown.
     *
     * @param strikes cantidad de strikes asignados al usuario
     * @param cooldownHasta fecha y hora de finalización del cooldown
     * @throws IllegalArgumentException si la cantidad de strikes es menor o igual a cero
     */
    public Sancion(int strikes, LocalDateTime cooldownHasta) {
        if (strikes <= 0) {
            throw new IllegalArgumentException("Los strikes deben ser mayores a 0.");
        }
        this.id = UUID.randomUUID();
        this.strikes = strikes;
        this.cooldownHasta = cooldownHasta;
    }

    /**
     * Obtiene el identificador único de la sanción.
     *
     * @return identificador de la sanción
     */
    public UUID getId() {
        return id;
    }

    /**
     * Obtiene la cantidad de strikes asociados a la sanción.
     *
     * @return número de strikes asignados
     */
    public int getStrikes() {
        return strikes;
    }

    /**
     * Obtiene la fecha y hora de finalización del cooldown.
     *
     * @return fecha de finalización o null si no se estableció un cooldown
     */
    public LocalDateTime getCooldownHasta() {
        return cooldownHasta;
    }

    /**
     * Verifica si el cooldown de la sanción está activo en el momento actual.
     *
     * @return true si el cooldown continúa vigente; false en caso contrario
     */
    public boolean estaActiva() {
        return estaActiva(LocalDateTime.now());
    }

    /**
     * Verifica si el cooldown de la sanción está activo en un momento determinado.
     *
     * @param momento fecha y hora en la que se evalúa la vigencia
     * @return true si el momento es anterior a la finalización del cooldown;
     *         false en caso contrario
     * @throws NullPointerException si el momento es nulo y existe un cooldown definido
     */
    public boolean estaActiva(LocalDateTime momento) {
        return cooldownHasta != null && momento.isBefore(cooldownHasta);
    }

    /**
     * Compara dos sanciones según su identificador único.
     *
     * @param o objeto con el que se realiza la comparación
     * @return true si ambas sanciones tienen el mismo identificador;
     *         false en caso contrario
     */
    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Sancion s && id.equals(s.id));
    }

    /**
     * Calcula el código hash a partir del identificador único.
     *
     * @return código hash de la sanción
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

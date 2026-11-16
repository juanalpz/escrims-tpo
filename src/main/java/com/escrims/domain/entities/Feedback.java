package com.escrims.domain.entities;

import java.util.Objects;
import java.util.UUID;

/**
 * Representa el feedback de un jugador al finalizar un scrim,
 * compuesto por un comentario, una puntuación y un estado de moderación.
 */
public class Feedback {

    private final UUID id;
    private final String comentario;
    private final int puntuacion;
    private EstadoReporte estado;

    /**
     * Crea un feedback con estado de moderación pendiente.
     *
     * @param comentario comentario realizado por el jugador
     * @param puntuacion valoración del scrim entre 1 y 5
     * @throws IllegalArgumentException si la puntuación está fuera del rango permitido
     */
    public Feedback(String comentario, int puntuacion) {
        if (puntuacion < 1 || puntuacion > 5) {
            throw new IllegalArgumentException("La puntuación debe estar entre 1 y 5.");
        }
        this.id = UUID.randomUUID();
        this.comentario = comentario;
        this.puntuacion = puntuacion;
        this.estado = EstadoReporte.PENDIENTE;
    }

    /**
     * Actualiza el estado de moderación del feedback.
     *
     * @param nuevoEstado estado de moderación a asignar
     * @throws IllegalArgumentException si el nuevo estado es nulo
     */
    public void moderar(EstadoReporte nuevoEstado) {
        if (nuevoEstado == null) {
            throw new IllegalArgumentException("El estado de moderación no puede ser nulo.");
        }
        this.estado = nuevoEstado;
    }

    /**
     * Obtiene el identificador único del feedback.
     *
     * @return identificador del feedback
     */
    public UUID getId() { return id; }

    /**
     * Obtiene el comentario del jugador.
     *
     * @return comentario registrado
     */
    public String getComentario() { return comentario; }

    /**
     * Obtiene la puntuación asignada al scrim.
     *
     * @return puntuación entre 1 y 5
     */
    public int getPuntuacion() { return puntuacion; }

    /**
     * Obtiene el estado actual de moderación.
     *
     * @return estado de moderación del feedback
     */
    public EstadoReporte getEstado() { return estado; }

    /**
     * Compara dos feedbacks según su identificador único.
     *
     * @param o objeto con el que se realiza la comparación
     * @return true si ambos feedbacks tienen el mismo identificador;
     *         false en caso contrario
     */
    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Feedback f && id.equals(f.id));
    }

    /**
     * Calcula el código hash a partir del identificador único.
     *
     * @return código hash del feedback
     */
    @Override
    public int hashCode() { return Objects.hash(id); }
}

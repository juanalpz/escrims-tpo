package com.escrims.domain.entities;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Representa la solicitud de un jugador para incorporarse a un scrim
 * en un rol específico, validando sus condiciones de elegibilidad
 * (RN-01, RN-03, RN-04, RN-06).
 */
public class Postulacion {

    private final UUID id;
    private final Usuario usuario;
    private final Scrim scrim;
    private final RolJuego rolDeseado;
    private EstadoPostulacion estado;
    private final LocalDateTime fechaPostulacion;

    /**
     * Crea una postulación y determina su estado inicial según
     * las condiciones de elegibilidad del jugador.
     *
     * @param usuario jugador que realiza la postulación
     * @param scrim scrim al que desea incorporarse
     * @param rolDeseado rol de juego solicitado
     * @throws IllegalArgumentException si el usuario o el scrim son nulos
     */
    public Postulacion(Usuario usuario, Scrim scrim, RolJuego rolDeseado) {
        if (usuario == null || scrim == null) {
            throw new IllegalArgumentException("El usuario y el scrim son obligatorios para postularse.");
        }
        this.id = UUID.randomUUID();
        this.usuario = usuario;
        this.scrim = scrim;
        this.rolDeseado = rolDeseado;
        this.fechaPostulacion = LocalDateTime.now();

        // RN-01, RN-03, RN-04, RN-06: una postulación no elegible se rechaza automáticamente.
        if (!esElegibleParaScrim(usuario, scrim)) {
            this.estado = EstadoPostulacion.RECHAZADA;
        } else {
            this.estado = EstadoPostulacion.PENDIENTE;
        }
    }

    /**
     * Verifica los requisitos de elegibilidad del jugador:
     * correo verificado (RN-01), ausencia de cooldown activo (RN-06),
     * MMR permitido (RN-03) y latencia máxima configurada (RN-04).
     *
     * @param usuario jugador cuya elegibilidad se desea verificar
     * @param scrim scrim para el que se evalúa la postulación
     * @return true si cumple todos los requisitos; false en caso contrario
     */
    public static boolean esElegibleParaScrim(Usuario usuario, Scrim scrim) {
        if (usuario == null || scrim == null) {
            return false;
        }
        if (!usuario.isEmailVerificado()) {
            return false;
        }
        if (usuario.tieneCooldownActivo()) {
            return false;
        }
        if (!scrim.mmrDentroDeRango(usuario.getMmr())) {
            return false;
        }
        if (scrim.getLatenciaMax() != null && scrim.getLatenciaMax() > 0) {
            if (usuario.getLatenciaPromedio() > scrim.getLatenciaMax()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Acepta una postulación pendiente si el jugador continúa
     * cumpliendo los requisitos de elegibilidad.
     *
     * @throws IllegalStateException si la postulación no está pendiente
     *         o si el jugador dejó de cumplir los requisitos de elegibilidad
     */
    public void aceptar() {
        if (this.estado != EstadoPostulacion.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden aceptar postulaciones en estado PENDIENTE.");
        }
        if (!esElegibleParaScrim(this.usuario, this.scrim)) {
            this.estado = EstadoPostulacion.RECHAZADA;
            throw new IllegalStateException("El usuario ya no cumple los requisitos para ser aceptado.");
        }
        this.estado = EstadoPostulacion.ACEPTADA;
    }

    /**
     * Rechaza una postulación que se encuentra pendiente.
     *
     * @throws IllegalStateException si la postulación no está pendiente
     */
    public void rechazar() {
        if (this.estado != EstadoPostulacion.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden rechazar postulaciones en estado PENDIENTE.");
        }
        this.estado = EstadoPostulacion.RECHAZADA;
    }

    /**
     * Obtiene el identificador único de la postulación.
     *
     * @return identificador de la postulación
     */
    public UUID getId() { return id; }

    /**
     * Obtiene el jugador que realizó la postulación.
     *
     * @return usuario postulante
     */
    public Usuario getUsuario() { return usuario; }

    /**
     * Obtiene el scrim asociado a la postulación.
     *
     * @return scrim solicitado
     */
    public Scrim getScrim() { return scrim; }

    /**
     * Obtiene el rol de juego solicitado.
     *
     * @return rol deseado por el jugador
     */
    public RolJuego getRolDeseado() { return rolDeseado; }

    /**
     * Obtiene el estado actual de la postulación.
     *
     * @return estado de la postulación
     */
    public EstadoPostulacion getEstado() { return estado; }

    /**
     * Obtiene la fecha y hora de creación de la postulación.
     *
     * @return fecha y hora de la postulación
     */
    public LocalDateTime getFechaPostulacion() { return fechaPostulacion; }

    /**
     * Compara dos postulaciones según su identificador único.
     *
     * @param o objeto con el que se realiza la comparación
     * @return true si ambas postulaciones tienen el mismo identificador;
     *         false en caso contrario
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Postulacion that)) return false;
        return Objects.equals(id, that.id);
    }

    /**
     * Calcula el código hash a partir del identificador único.
     *
     * @return código hash de la postulación
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

package com.escrims.domain.entities;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Representa la solicitud de un jugador para unirse a un Scrim en un rol específico.
 * Cumple RN-01, RN-03, RN-04, RN-06.
 */
public class Postulacion {

    private final UUID id;
    private final Usuario usuario;
    private final Scrim scrim;
    private final RolJuego rolDeseado;
    private EstadoPostulacion estado;
    private final LocalDateTime fechaPostulacion;

    public Postulacion(Usuario usuario, Scrim scrim, RolJuego rolDeseado) {
        if (usuario == null || scrim == null) {
            throw new IllegalArgumentException("El usuario y el scrim son obligatorios para postularse.");
        }
        this.id = UUID.randomUUID();
        this.usuario = usuario;
        this.scrim = scrim;
        this.rolDeseado = rolDeseado;
        this.fechaPostulacion = LocalDateTime.now();

        // Si el usuario no cumple las condiciones de elegibilidad, nace rechazada automáticamente
        if (!esElegibleParaScrim(usuario, scrim)) {
            this.estado = EstadoPostulacion.RECHAZADA;
        } else {
            this.estado = EstadoPostulacion.PENDIENTE;
        }
    }

    /**
     * Valida si el usuario cumple los requisitos para postularse:
     * - RN-01: email verificado.
     * - RN-06: sin cooldown ni sanción activa.
     * - RN-03: MMR dentro de los límites del scrim.
     * - RN-04: latencia menor o igual a la máxima permitida (si está configurada).
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
     * Transiciona la postulación a ACEPTADA solo si estaba PENDIENTE y el usuario sigue siendo elegible.
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
     * Transiciona la postulación a RECHAZADA solo si estaba PENDIENTE.
     */
    public void rechazar() {
        if (this.estado != EstadoPostulacion.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden rechazar postulaciones en estado PENDIENTE.");
        }
        this.estado = EstadoPostulacion.RECHAZADA;
    }

    public UUID getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public Scrim getScrim() { return scrim; }
    public RolJuego getRolDeseado() { return rolDeseado; }
    public EstadoPostulacion getEstado() { return estado; }
    public LocalDateTime getFechaPostulacion() { return fechaPostulacion; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Postulacion that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

package com.escrims.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA que representa la confirmación de asistencia de un usuario
 * a un scrim, mapeada a la tabla {@code confirmaciones}.
 */
@Entity
@Table(name = "confirmaciones", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"scrim_id", "usuario_id"})
})
public class ConfirmacionJpaEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scrim_id", nullable = false)
    private ScrimJpaEntity scrim;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioJpaEntity usuario;

    @Column(nullable = false)
    private boolean confirmado;

    @Column(name = "fecha_confirmacion")
    private LocalDateTime fechaConfirmacion;

    /**
     * Constructor sin argumentos requerido por JPA.
     */
    protected ConfirmacionJpaEntity() {}

    /**
     * Crea una confirmación de asistencia inicialmente pendiente
     * para un usuario asociado a un scrim.
     *
     * @param scrim entidad JPA del scrim correspondiente
     * @param usuario entidad JPA del usuario que debe confirmar su asistencia
     */
    public ConfirmacionJpaEntity(ScrimJpaEntity scrim, UsuarioJpaEntity usuario) {
        this.id = UUID.randomUUID();
        this.scrim = scrim;
        this.usuario = usuario;
        this.confirmado = false;
    }

    /**
     * Registra la confirmación de asistencia del usuario,
     * estableciendo su estado y la fecha y hora actuales.
     */
    public void confirmar() {
        this.confirmado = true;
        this.fechaConfirmacion = LocalDateTime.now();
    }

    /**
     * Obtiene el identificador único de la confirmación.
     *
     * @return identificador de la confirmación
     */
    public UUID getId() { return id; }

    /**
     * Obtiene el scrim asociado a la confirmación.
     *
     * @return entidad JPA del scrim
     */
    public ScrimJpaEntity getScrim() { return scrim; }

    /**
     * Obtiene el usuario asociado a la confirmación.
     *
     * @return entidad JPA del usuario
     */
    public UsuarioJpaEntity getUsuario() { return usuario; }

    /**
     * Indica si el usuario confirmó su asistencia.
     *
     * @return true si la asistencia fue confirmada; false en caso contrario
     */
    public boolean isConfirmado() { return confirmado; }

    /**
     * Obtiene la fecha y hora de confirmación de asistencia.
     *
     * @return fecha y hora de confirmación o null si aún no fue confirmada
     */
    public LocalDateTime getFechaConfirmacion() { return fechaConfirmacion; }
}

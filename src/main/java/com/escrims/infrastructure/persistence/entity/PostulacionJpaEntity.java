package com.escrims.infrastructure.persistence.entity;

import com.escrims.domain.entities.EstadoPostulacion;
import com.escrims.domain.entities.RolJuego;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA que representa las solicitudes de ingreso de jugadores
 * a un scrim, mapeada a la tabla {@code postulaciones}.
 */
@Entity
@Table(name = "postulaciones", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"scrim_id", "usuario_id"})
})
public class PostulacionJpaEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scrim_id", nullable = false)
    private ScrimJpaEntity scrim;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioJpaEntity usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol_deseado", nullable = false)
    private RolJuego rolDeseado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPostulacion estado;

    @Column(name = "fecha_postulacion", nullable = false)
    private LocalDateTime fechaPostulacion;

    /**
     * Constructor sin argumentos requerido por JPA.
     */
    protected PostulacionJpaEntity() {}

    /**
     * Crea una postulación con estado inicial pendiente y registra
     * la fecha y hora de su creación.
     *
     * @param scrim entidad JPA del scrim al que se solicita ingresar
     * @param usuario entidad JPA del usuario postulante
     * @param rolDeseado rol de juego solicitado por el usuario
     */
    public PostulacionJpaEntity(ScrimJpaEntity scrim, UsuarioJpaEntity usuario, RolJuego rolDeseado) {
        this.id = UUID.randomUUID();
        this.scrim = scrim;
        this.usuario = usuario;
        this.rolDeseado = rolDeseado;
        this.estado = EstadoPostulacion.PENDIENTE;
        this.fechaPostulacion = LocalDateTime.now();
    }

    /**
     * Obtiene el identificador único de la postulación.
     *
     * @return identificador de la postulación
     */
    public UUID getId() { return id; }

    /**
     * Obtiene el scrim asociado a la postulación.
     *
     * @return entidad JPA del scrim
     */
    public ScrimJpaEntity getScrim() { return scrim; }

    /**
     * Obtiene el usuario que realizó la postulación.
     *
     * @return entidad JPA del usuario postulante
     */
    public UsuarioJpaEntity getUsuario() { return usuario; }

    /**
     * Obtiene el rol de juego solicitado por el usuario.
     *
     * @return rol deseado en el scrim
     */
    public RolJuego getRolDeseado() { return rolDeseado; }

    /**
     * Obtiene el estado actual de la postulación.
     *
     * @return estado de la postulación
     */
    public EstadoPostulacion getEstado() { return estado; }

    /**
     * Actualiza el estado de la postulación.
     *
     * @param estado nuevo estado de la postulación
     */
    public void setEstado(EstadoPostulacion estado) { this.estado = estado; }

    /**
     * Obtiene la fecha y hora de creación de la postulación.
     *
     * @return fecha y hora en que se realizó la postulación
     */
    public LocalDateTime getFechaPostulacion() { return fechaPostulacion; }
}

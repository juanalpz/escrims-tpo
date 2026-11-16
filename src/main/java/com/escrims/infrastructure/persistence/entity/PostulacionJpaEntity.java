package com.escrims.infrastructure.persistence.entity;

import com.escrims.domain.entities.EstadoPostulacion;
import com.escrims.domain.entities.RolJuego;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

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

    protected PostulacionJpaEntity() {}

    public PostulacionJpaEntity(ScrimJpaEntity scrim, UsuarioJpaEntity usuario, RolJuego rolDeseado) {
        this.id = UUID.randomUUID();
        this.scrim = scrim;
        this.usuario = usuario;
        this.rolDeseado = rolDeseado;
        this.estado = EstadoPostulacion.PENDIENTE;
        this.fechaPostulacion = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public ScrimJpaEntity getScrim() { return scrim; }
    public UsuarioJpaEntity getUsuario() { return usuario; }
    public RolJuego getRolDeseado() { return rolDeseado; }
    public EstadoPostulacion getEstado() { return estado; }
    public void setEstado(EstadoPostulacion estado) { this.estado = estado; }
    public LocalDateTime getFechaPostulacion() { return fechaPostulacion; }
}

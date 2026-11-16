package com.escrims.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

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

    protected ConfirmacionJpaEntity() {}

    public ConfirmacionJpaEntity(ScrimJpaEntity scrim, UsuarioJpaEntity usuario) {
        this.id = UUID.randomUUID();
        this.scrim = scrim;
        this.usuario = usuario;
        this.confirmado = false;
    }

    public void confirmar() {
        this.confirmado = true;
        this.fechaConfirmacion = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public ScrimJpaEntity getScrim() { return scrim; }
    public UsuarioJpaEntity getUsuario() { return usuario; }
    public boolean isConfirmado() { return confirmado; }
    public LocalDateTime getFechaConfirmacion() { return fechaConfirmacion; }
}

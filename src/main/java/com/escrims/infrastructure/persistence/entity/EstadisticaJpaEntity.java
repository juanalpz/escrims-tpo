package com.escrims.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "estadisticas", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"scrim_id", "usuario_id"})
})
public class EstadisticaJpaEntity {

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
    private boolean mvp;

    @Column(nullable = false, length = 30)
    private String kda;

    @Column(length = 500)
    private String observaciones;

    protected EstadisticaJpaEntity() {}

    public EstadisticaJpaEntity(ScrimJpaEntity scrim, UsuarioJpaEntity usuario, boolean mvp, String kda, String observaciones) {
        this.id = UUID.randomUUID();
        this.scrim = scrim;
        this.usuario = usuario;
        this.mvp = mvp;
        this.kda = kda;
        this.observaciones = observaciones;
    }

    public UUID getId() { return id; }
    public ScrimJpaEntity getScrim() { return scrim; }
    public UsuarioJpaEntity getUsuario() { return usuario; }
    public boolean isMvp() { return mvp; }
    public String getKda() { return kda; }
    public String getObservaciones() { return observaciones; }
}

package com.escrims.infrastructure.persistence.entity;

import com.escrims.domain.entities.EstadoReporte;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reportes_conducta")
public class ReporteConductaJpaEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scrim_id", nullable = false)
    private ScrimJpaEntity scrim;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reportado_id", nullable = false)
    private UsuarioJpaEntity reportado;

    @Column(nullable = false, length = 300)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReporte estado;

    @Column(length = 200)
    private String sancionAplicada;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    protected ReporteConductaJpaEntity() {}

    public ReporteConductaJpaEntity(ScrimJpaEntity scrim, UsuarioJpaEntity reportado, String motivo) {
        this.id = UUID.randomUUID();
        this.scrim = scrim;
        this.reportado = reportado;
        this.motivo = motivo;
        this.estado = EstadoReporte.PENDIENTE;
        this.creadoEn = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public ScrimJpaEntity getScrim() { return scrim; }
    public UsuarioJpaEntity getReportado() { return reportado; }
    public String getMotivo() { return motivo; }
    public EstadoReporte getEstado() { return estado; }
    public void setEstado(EstadoReporte estado) { this.estado = estado; }
    public String getSancionAplicada() { return sancionAplicada; }
    public void setSancionAplicada(String sancionAplicada) { this.sancionAplicada = sancionAplicada; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
}

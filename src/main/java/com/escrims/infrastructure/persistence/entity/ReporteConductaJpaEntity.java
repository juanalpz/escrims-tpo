package com.escrims.infrastructure.persistence.entity;

import com.escrims.domain.entities.EstadoReporte;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA que representa los reportes de conducta del sistema
 * de moderación, mapeada a la tabla {@code reportes_conducta}.
 */
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

    /**
     * Constructor sin argumentos requerido por JPA.
     */
    protected ReporteConductaJpaEntity() {}

    /**
     * Crea un reporte de conducta con estado inicial pendiente
     * y registra la fecha y hora de su creación.
     *
     * @param scrim entidad JPA del scrim donde ocurrió la conducta reportada
     * @param reportado entidad JPA del usuario denunciado
     * @param motivo descripción de la conducta reportada
     */
    public ReporteConductaJpaEntity(ScrimJpaEntity scrim, UsuarioJpaEntity reportado, String motivo) {
        this.id = UUID.randomUUID();
        this.scrim = scrim;
        this.reportado = reportado;
        this.motivo = motivo;
        this.estado = EstadoReporte.PENDIENTE;
        this.creadoEn = LocalDateTime.now();
    }

    /**
     * Obtiene el identificador único del reporte.
     *
     * @return identificador del reporte de conducta
     */
    public UUID getId() { return id; }

    /**
     * Obtiene el scrim asociado al reporte.
     *
     * @return entidad JPA del scrim
     */
    public ScrimJpaEntity getScrim() { return scrim; }

    /**
     * Obtiene el usuario denunciado.
     *
     * @return entidad JPA del usuario reportado
     */
    public UsuarioJpaEntity getReportado() { return reportado; }

    /**
     * Obtiene el motivo del reporte de conducta.
     *
     * @return descripción de la conducta reportada
     */
    public String getMotivo() { return motivo; }

    /**
     * Obtiene el estado actual del reporte.
     *
     * @return estado de moderación del reporte
     */
    public EstadoReporte getEstado() { return estado; }

    /**
     * Actualiza el estado de moderación del reporte.
     *
     * @param estado nuevo estado del reporte
     */
    public void setEstado(EstadoReporte estado) { this.estado = estado; }

    /**
     * Obtiene la descripción de la sanción aplicada al usuario reportado.
     *
     * @return sanción registrada o null si no se asignó ninguna
     */
    public String getSancionAplicada() { return sancionAplicada; }

    /**
     * Establece la descripción de la sanción asociada al reporte.
     *
     * @param sancionAplicada descripción de la sanción aplicada
     */
    public void setSancionAplicada(String sancionAplicada) { this.sancionAplicada = sancionAplicada; }

    /**
     * Obtiene la fecha y hora de creación del reporte.
     *
     * @return fecha y hora en que se registró el reporte
     */
    public LocalDateTime getCreadoEn() { return creadoEn; }
}

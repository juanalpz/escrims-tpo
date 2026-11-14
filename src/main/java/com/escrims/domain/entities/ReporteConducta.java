package com.escrims.domain.entities;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Representa una denuncia de conducta (abandono, toxicidad, no-show) sobre un usuario en un Scrim.
 * Es procesada por la cadena de responsabilidad de moderación (Auto -> Bot -> Humano).
 */
public class ReporteConducta {

    private final UUID id;
    private final Scrim scrim;
    private final Usuario reportante;
    private final Usuario reportado;
    private final String motivo;
    private EstadoReporte estado;
    private Sancion sancion;
    private final LocalDateTime fechaCreacion;

    public ReporteConducta(Scrim scrim, Usuario reportante, Usuario reportado, String motivo) {
        if (scrim == null || reportante == null || reportado == null) {
            throw new IllegalArgumentException("El scrim, el reportante y el reportado son obligatorios.");
        }
        if (reportante.equals(reportado)) {
            throw new IllegalArgumentException("Un usuario no puede reportarse a sí mismo.");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo del reporte no puede estar vacío.");
        }
        this.id = UUID.randomUUID();
        this.scrim = scrim;
        this.reportante = reportante;
        this.reportado = reportado;
        this.motivo = motivo;
        this.estado = EstadoReporte.PENDIENTE;
        this.fechaCreacion = LocalDateTime.now();
    }

    /**
     * Marca el reporte como en revisión (usado al escalar al moderador humano).
     */
    public void escalarARevision() {
        if (this.estado != EstadoReporte.PENDIENTE) {
            throw new IllegalStateException("Solo se puede escalar reportes en estado PENDIENTE.");
        }
        this.estado = EstadoReporte.EN_REVISION;
    }

    /**
     * Resuelve el reporte asociando una sanción aplicada (strike/cooldown).
     */
    public void resolverConSancion(Sancion sancion) {
        if (sancion == null) {
            throw new IllegalArgumentException("Debe indicar la sanción aplicada al resolver.");
        }
        if (this.estado == EstadoReporte.RESUELTO || this.estado == EstadoReporte.RECHAZADO) {
            throw new IllegalStateException("No se puede resolver un reporte ya finalizado.");
        }
        this.sancion = sancion;
        this.estado = EstadoReporte.RESUELTO;
        this.reportado.agregarSancion(sancion);
    }

    /**
     * Desestima el reporte sin aplicar sanción.
     */
    public void rechazar() {
        if (this.estado == EstadoReporte.RESUELTO || this.estado == EstadoReporte.RECHAZADO) {
            throw new IllegalStateException("No se puede rechazar un reporte ya finalizado.");
        }
        this.estado = EstadoReporte.RECHAZADO;
    }

    public UUID getId() { return id; }
    public Scrim getScrim() { return scrim; }
    public Usuario getReportante() { return reportante; }
    public Usuario getReportado() { return reportado; }
    public String getMotivo() { return motivo; }
    public EstadoReporte getEstado() { return estado; }
    public Sancion getSancion() { return sancion; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReporteConducta that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

package com.escrims.domain.entities;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Representa una denuncia de conducta de un jugador durante un scrim,
 * gestionada mediante el patrón Chain of Responsibility de moderación.
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

    /**
     * Crea un reporte de conducta con estado inicial pendiente.
     *
     * @param scrim scrim en el que ocurrió la conducta reportada
     * @param reportante usuario que realiza la denuncia
     * @param reportado usuario sobre el que se realiza la denuncia
     * @param motivo descripción de la conducta reportada
     * @throws IllegalArgumentException si el scrim, el reportante o el reportado
     *         son nulos, si ambos usuarios son iguales o si el motivo está vacío
     */
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
     * Escala un reporte pendiente a revisión por un moderador humano.
     *
     * @throws IllegalStateException si el reporte no está en estado pendiente
     */
    public void escalarARevision() {
        if (this.estado != EstadoReporte.PENDIENTE) {
            throw new IllegalStateException("Solo se puede escalar reportes en estado PENDIENTE.");
        }
        this.estado = EstadoReporte.EN_REVISION;
    }

    /**
     * Resuelve el reporte y registra la sanción aplicada al usuario reportado.
     *
     * @param sancion sanción que se aplica al usuario reportado
     * @throws IllegalArgumentException si la sanción es nula
     * @throws IllegalStateException si el reporte ya fue resuelto o rechazado
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
     * Desestima un reporte sin aplicar sanciones al usuario reportado.
     *
     * @throws IllegalStateException si el reporte ya fue resuelto o rechazado
     */
    public void rechazar() {
        if (this.estado == EstadoReporte.RESUELTO || this.estado == EstadoReporte.RECHAZADO) {
            throw new IllegalStateException("No se puede rechazar un reporte ya finalizado.");
        }
        this.estado = EstadoReporte.RECHAZADO;
    }

    /**
     * Obtiene el identificador único del reporte.
     *
     * @return identificador del reporte
     */
    public UUID getId() { return id; }

    /**
     * Obtiene el scrim asociado al reporte.
     *
     * @return scrim en el que ocurrió la conducta reportada
     */
    public Scrim getScrim() { return scrim; }

    /**
     * Obtiene el usuario que realizó la denuncia.
     *
     * @return usuario reportante
     */
    public Usuario getReportante() { return reportante; }

    /**
     * Obtiene el usuario denunciado.
     *
     * @return usuario reportado
     */
    public Usuario getReportado() { return reportado; }

    /**
     * Obtiene el motivo de la denuncia.
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
     * Obtiene la sanción asociada al reporte.
     *
     * @return sanción aplicada o null si no se asignó ninguna
     */
    public Sancion getSancion() { return sancion; }

    /**
     * Obtiene la fecha y hora de creación del reporte.
     *
     * @return fecha y hora de registro de la denuncia
     */
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }

    /**
     * Compara dos reportes según su identificador único.
     *
     * @param o objeto con el que se realiza la comparación
     * @return true si ambos reportes tienen el mismo identificador;
     *         false en caso contrario
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReporteConducta that)) return false;
        return Objects.equals(id, that.id);
    }

    /**
     * Calcula el código hash a partir del identificador único.
     *
     * @return código hash del reporte
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

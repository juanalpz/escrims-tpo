package com.escrims.infrastructure.persistence.memory;

import com.escrims.domain.entities.EstadoReporte;
import com.escrims.domain.entities.ReporteConducta;
import com.escrims.domain.repository.ReporteConductaRepository;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementación en memoria del repositorio de reportes de conducta.
 */
@Repository
public class InMemoryReporteConductaRepository implements ReporteConductaRepository {

    private final Map<UUID, ReporteConducta> reportes = new ConcurrentHashMap<>();

    @Override
    public void guardar(ReporteConducta reporte) {
        reportes.put(reporte.getId(), reporte);
    }

    @Override
    public List<ReporteConducta> buscarPendientes() {
        return buscarPorEstado(EstadoReporte.PENDIENTE);
    }

    @Override
    public void actualizar(ReporteConducta reporte) {
        guardar(reporte);
    }

    @Override
    public Optional<ReporteConducta> buscarPorId(UUID id) {
        return id == null ? Optional.empty() : Optional.ofNullable(reportes.get(id));
    }

    @Override
    public List<ReporteConducta> buscarPorEstado(EstadoReporte estado) {
        return reportes.values().stream()
                .filter(r -> estado == null || r.getEstado() == estado)
                .sorted(Comparator.comparing(ReporteConducta::getFechaCreacion))
                .toList();
    }

    @Override
    public int contarContraUsuario(UUID reportadoId) {
        return (int) reportes.values().stream()
                .filter(r -> r.getReportado().getId().equals(reportadoId))
                .count();
    }

    @Override
    public boolean existeReporte(UUID scrimId, UUID reportanteId, UUID reportadoId) {
        return reportes.values().stream().anyMatch(r -> r.getScrim().getId().equals(scrimId)
                && r.getReportante().getId().equals(reportanteId)
                && r.getReportado().getId().equals(reportadoId));
    }
}

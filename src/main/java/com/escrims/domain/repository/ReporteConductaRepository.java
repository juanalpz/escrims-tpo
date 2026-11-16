package com.escrims.domain.repository;

import com.escrims.domain.entities.ReporteConducta;

import java.util.List;

/**
 * Define el contrato de persistencia de reportes de conducta
 * mediante el patrón Repository.
 */
public interface ReporteConductaRepository {

    /**
     * Guarda un reporte de conducta en el repositorio.
     *
     * @param reporte reporte de conducta que se desea persistir
     */
    void guardar(ReporteConducta reporte);

    /**
     * Obtiene los reportes de conducta pendientes de moderación.
     *
     * @return lista de reportes en estado pendiente
     */
    List<ReporteConducta> buscarPendientes();

    /**
     * Actualiza un reporte de conducta existente en el repositorio.
     *
     * @param reporte reporte de conducta con los datos actualizados
     */
    void actualizar(ReporteConducta reporte);
}

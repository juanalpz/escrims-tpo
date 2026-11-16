package com.escrims.domain.repository;

import com.escrims.domain.entities.EstadoReporte;
import com.escrims.domain.entities.ReporteConducta;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    /**
     * Busca un reporte a partir de su identificador único.
     *
     * @param id identificador del reporte
     * @return Optional con el reporte encontrado o vacío si no existe
     */
    Optional<ReporteConducta> buscarPorId(UUID id);

    /**
     * Obtiene los reportes que se encuentran en un estado determinado.
     *
     * @param estado estado de moderación buscado, o null para traer todos
     * @return lista de reportes ordenada por fecha de creación
     */
    List<ReporteConducta> buscarPorEstado(EstadoReporte estado);

    /**
     * Cuenta los reportes registrados contra un usuario.
     *
     * @param reportadoId identificador del usuario reportado
     * @return cantidad de reportes almacenados contra ese usuario
     */
    int contarContraUsuario(UUID reportadoId);

    /**
     * Verifica si un usuario ya reportó a otro por el mismo scrim.
     *
     * @param scrimId identificador del scrim
     * @param reportanteId identificador de quien reporta
     * @param reportadoId identificador del reportado
     * @return true si ya existe un reporte con esa combinación
     */
    boolean existeReporte(UUID scrimId, UUID reportanteId, UUID reportadoId);
}

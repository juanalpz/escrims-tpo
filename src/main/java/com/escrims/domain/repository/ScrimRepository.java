package com.escrims.domain.repository;

import com.escrims.domain.entities.DatosScrim;
import com.escrims.domain.entities.Scrim;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Define el contrato de persistencia y consulta de scrims
 * mediante el patrón Repository, con implementaciones en infraestructura.
 */
public interface ScrimRepository {

    /**
     * Guarda un scrim en el repositorio.
     *
     * @param scrim scrim que se desea persistir
     */
    void guardar(Scrim scrim);

    /**
     * Busca un scrim a partir de su identificador único.
     *
     * @param id identificador del scrim que se desea buscar
     * @return Optional con el scrim encontrado o vacío si no existe
     */
    Optional<Scrim> buscarPorId(UUID id);

    /**
     * Busca los scrims disponibles según los criterios de filtrado indicados.
     *
     * @param filtro criterios de búsqueda de scrims
     * @return lista de scrims disponibles que cumplen los criterios
     */
    List<Scrim> buscarDisponibles(DatosScrim filtro);

    /**
     * Elimina un scrim del repositorio mediante su identificador único.
     *
     * @param id identificador del scrim que se desea eliminar
     */
    void eliminar(UUID id);
}

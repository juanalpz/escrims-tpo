package com.escrims.domain.repository;

import com.escrims.domain.entities.RegistroIncidencia;

import java.util.List;
import java.util.UUID;

/**
 * Define el contrato de persistencia del registro de abandonos y no-shows
 * mediante el patrón Repository.
 */
public interface RegistroIncidenciaRepository {

    /**
     * Guarda una incidencia en el repositorio.
     *
     * @param incidencia incidencia que se desea persistir
     */
    void guardar(RegistroIncidencia incidencia);

    /**
     * Obtiene las incidencias registradas de un usuario, de la más antigua a la más reciente.
     *
     * @param usuarioId identificador del usuario
     * @return lista de incidencias del usuario
     */
    List<RegistroIncidencia> buscarPorUsuario(UUID usuarioId);

    /**
     * Verifica si un usuario ya tiene una incidencia registrada en un scrim.
     *
     * @param scrimId identificador del scrim
     * @param usuarioId identificador del usuario
     * @return true si ya se registró un abandono o no-show de ese usuario en ese scrim
     */
    boolean existe(UUID scrimId, UUID usuarioId);
}

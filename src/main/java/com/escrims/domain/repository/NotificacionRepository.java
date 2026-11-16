package com.escrims.domain.repository;

import com.escrims.domain.entities.Notificacion;

import java.util.List;

/**
 * Define el contrato de persistencia de notificaciones
 * mediante el patrón Repository.
 */
public interface NotificacionRepository {

    /**
     * Guarda una notificación en el repositorio.
     *
     * @param notificacion notificación que se desea persistir
     */
    void guardar(Notificacion notificacion);

    /**
     * Obtiene las notificaciones pendientes de envío.
     *
     * @return lista de notificaciones en estado pendiente
     */
    List<Notificacion> buscarPendientes();

    /**
     * Actualiza una notificación existente en el repositorio.
     *
     * @param notificacion notificación con los datos actualizados
     */
    void actualizar(Notificacion notificacion);
}

package com.escrims.infrastructure.persistence.repository;

import com.escrims.domain.entities.EstadoPostulacion;
import com.escrims.infrastructure.persistence.entity.PostulacionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

/**
 * Define el repositorio de persistencia JPA para las postulaciones
 * de jugadores mediante Spring Data y el patrón Repository.
 */
public interface PostulacionJpaRepository extends JpaRepository<PostulacionJpaEntity, UUID> {

    /**
     * Busca las postulaciones asociadas a un scrim.
     *
     * @param scrimId identificador del scrim cuyas postulaciones se desean consultar
     * @return lista de postulaciones correspondientes al scrim
     */
    List<PostulacionJpaEntity> findByScrimId(UUID scrimId);

    /**
     * Cuenta las postulaciones de un scrim que se encuentran
     * en un estado determinado.
     *
     * @param scrimId identificador del scrim cuyas postulaciones se desean contar
     * @param estado estado de postulación utilizado como criterio de búsqueda
     * @return cantidad de postulaciones que coinciden con los criterios indicados
     */
    long countByScrimIdAndEstado(UUID scrimId, EstadoPostulacion estado);
}

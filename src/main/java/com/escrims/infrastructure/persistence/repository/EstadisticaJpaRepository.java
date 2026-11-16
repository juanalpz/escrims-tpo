package com.escrims.infrastructure.persistence.repository;

import com.escrims.infrastructure.persistence.entity.EstadisticaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

/**
 * Define el repositorio de persistencia JPA para las estadísticas
 * individuales de jugadores mediante Spring Data y el patrón Repository.
 */
public interface EstadisticaJpaRepository extends JpaRepository<EstadisticaJpaEntity, UUID> {

    /**
     * Busca las estadísticas individuales registradas para un scrim.
     *
     * @param scrimId identificador del scrim cuyas estadísticas se desean consultar
     * @return lista de estadísticas correspondientes al scrim
     */
    List<EstadisticaJpaEntity> findByScrimId(UUID scrimId);
}

package com.escrims.infrastructure.persistence.repository;

import com.escrims.infrastructure.persistence.entity.ConfirmacionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

/**
 * Define el repositorio de persistencia JPA para las confirmaciones
 * de asistencia mediante Spring Data y el patrón Repository.
 */
public interface ConfirmacionJpaRepository extends JpaRepository<ConfirmacionJpaEntity, UUID> {

    /**
     * Busca las confirmaciones de asistencia asociadas a un scrim.
     *
     * @param scrimId identificador del scrim cuyas confirmaciones se desean consultar
     * @return lista de confirmaciones correspondientes al scrim
     */
    List<ConfirmacionJpaEntity> findByScrimId(UUID scrimId);
}

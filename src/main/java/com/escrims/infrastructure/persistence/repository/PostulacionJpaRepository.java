package com.escrims.infrastructure.persistence.repository;

import com.escrims.domain.entities.EstadoPostulacion;
import com.escrims.infrastructure.persistence.entity.PostulacionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface PostulacionJpaRepository extends JpaRepository<PostulacionJpaEntity, UUID> {
    List<PostulacionJpaEntity> findByScrimId(UUID scrimId);
    long countByScrimIdAndEstado(UUID scrimId, EstadoPostulacion estado);
}

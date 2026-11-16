package com.escrims.infrastructure.persistence.repository;

import com.escrims.infrastructure.persistence.entity.ConfirmacionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ConfirmacionJpaRepository extends JpaRepository<ConfirmacionJpaEntity, UUID> {
    List<ConfirmacionJpaEntity> findByScrimId(UUID scrimId);
}

package com.escrims.infrastructure.persistence.repository;

import com.escrims.domain.entities.Juego;
import com.escrims.domain.entities.Region;
import com.escrims.infrastructure.persistence.entity.ScrimJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ScrimJpaRepository extends JpaRepository<ScrimJpaEntity, UUID> {
    List<ScrimJpaEntity> findByEstadoActualIn(List<String> estados);
    List<ScrimJpaEntity> findByJuegoAndRegionAndLatenciaMaxLessThanEqual(Juego juego, Region region, int latenciaMax);
}

package com.escrims.infrastructure.persistence.repository;

import com.escrims.domain.entities.EstadoReporte;
import com.escrims.infrastructure.persistence.entity.ReporteConductaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ReporteConductaJpaRepository extends JpaRepository<ReporteConductaJpaEntity, UUID> {
    List<ReporteConductaJpaEntity> findByEstado(EstadoReporte estado);
}

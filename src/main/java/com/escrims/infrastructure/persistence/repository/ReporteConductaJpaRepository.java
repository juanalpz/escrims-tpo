package com.escrims.infrastructure.persistence.repository;

import com.escrims.domain.entities.EstadoReporte;
import com.escrims.infrastructure.persistence.entity.ReporteConductaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

/**
 * Define el repositorio de persistencia JPA para los reportes de conducta
 * mediante Spring Data y el patrón Repository.
 */
public interface ReporteConductaJpaRepository extends JpaRepository<ReporteConductaJpaEntity, UUID> {

    /**
     * Busca los reportes de conducta que se encuentran en un estado determinado.
     *
     * @param estado estado de moderación utilizado como criterio de búsqueda
     * @return lista de reportes de conducta que coinciden con el estado indicado
     */
    List<ReporteConductaJpaEntity> findByEstado(EstadoReporte estado);
}

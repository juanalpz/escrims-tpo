package com.escrims.infrastructure.persistence.repository;

import com.escrims.domain.entities.Juego;
import com.escrims.domain.entities.Region;
import com.escrims.infrastructure.persistence.entity.ScrimJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

/**
 * Define el repositorio de persistencia JPA para los scrims
 * mediante Spring Data y el patrón Repository.
 */
public interface ScrimJpaRepository extends JpaRepository<ScrimJpaEntity, UUID> {

    /**
     * Busca los scrims cuyo estado actual coincide con alguno
     * de los estados indicados.
     *
     * @param estados lista de estados utilizados como criterio de búsqueda
     * @return lista de scrims que se encuentran en los estados especificados
     */
    List<ScrimJpaEntity> findByEstadoActualIn(List<String> estados);

    /**
     * Busca los scrims que coinciden con un juego y una región,
     * cuya latencia máxima configurada no supera el límite indicado.
     *
     * @param juego juego utilizado como criterio de búsqueda
     * @param region región del servidor de juego
     * @param latenciaMax límite superior de latencia en milisegundos
     * @return lista de scrims que cumplen los criterios de juego,
     *         región y latencia máxima
     */
    List<ScrimJpaEntity> findByJuegoAndRegionAndLatenciaMaxLessThanEqual(Juego juego, Region region, int latenciaMax);
}

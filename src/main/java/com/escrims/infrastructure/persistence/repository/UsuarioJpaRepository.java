package com.escrims.infrastructure.persistence.repository;

import com.escrims.infrastructure.persistence.entity.UsuarioJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

/**
 * Define el repositorio de persistencia JPA para las cuentas y perfiles
 * de usuarios mediante Spring Data y el patrón Repository.
 */
public interface UsuarioJpaRepository extends JpaRepository<UsuarioJpaEntity, UUID> {

    /**
     * Busca un usuario a partir de su dirección de correo electrónico.
     *
     * @param email dirección de correo electrónico utilizada como criterio de búsqueda
     * @return Optional con el usuario encontrado o vacío si no existe
     */
    Optional<UsuarioJpaEntity> findByEmail(String email);

    /**
     * Busca un usuario a partir de su nombre de usuario.
     *
     * @param username nombre de usuario utilizado como criterio de búsqueda
     * @return Optional con el usuario encontrado o vacío si no existe
     */
    Optional<UsuarioJpaEntity> findByUsername(String username);

    /**
     * Verifica si existe un usuario registrado con el correo electrónico indicado.
     *
     * @param email dirección de correo electrónico que se desea verificar
     * @return true si existe un usuario con ese correo; false en caso contrario
     */
    boolean existsByEmail(String email);
}
